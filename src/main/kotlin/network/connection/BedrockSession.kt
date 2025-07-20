package org.chorus_oss.chorus.network.connection

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import com.github.oxo42.stateless4j.delegates.Action
import dev.whyoleg.cryptography.algorithms.AES
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.util.internal.PlatformDependent
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.data.CommandDataVersions
import org.chorus_oss.chorus.event.player.PlayerCreationEvent
import org.chorus_oss.chorus.event.server.DataPacketDecodeEvent
import org.chorus_oss.chorus.event.server.PacketReceiveEvent
import org.chorus_oss.chorus.event.server.PacketSendEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.network.PacketHandler
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper
import org.chorus_oss.chorus.network.connection.netty.codec.packet.BedrockPacketCodec
import org.chorus_oss.chorus.network.process.PacketManager
import org.chorus_oss.chorus.network.process.SessionState
import org.chorus_oss.chorus.network.process.handler.*
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PlayerInfo
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.registry.CreativeItemRegistry
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.ByteBufVarInt
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.PacketRegistry
import org.chorus_oss.protocol.packets.CraftingDataPacket
import org.chorus_oss.protocol.packets.LoginPacket
import org.chorus_oss.protocol.packets.PlayerSkinPacket
import org.chorus_oss.protocol.types.DisconnectFailReason
import org.jetbrains.annotations.ApiStatus
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.SecretKey

class BedrockSession(val peer: BedrockPeer, val subClientId: Int) : Loggable {
    private val closed = AtomicBoolean()
    private val inbound: Queue<Packet> = PlatformDependent.newSpscQueue()
    private val nettyThreadOwned = AtomicBoolean(false)
    private val consumer = AtomicReference<((Packet) -> Unit)?>(null)

    @JvmField
    val machine: StateMachine<SessionState, SessionState>
    var player: Player? = null
        private set
    private var info: PlayerInfo? = null
    protected var packetHandler: PacketHandler? = null

    @JvmField
    var address: InetSocketAddress?

    var authenticated: Boolean = false
        private set


    init {
        // from session start to log in sequence complete, netty threads own the session
        this.setNettyThreadOwned(true)

        this.setPacketConsumer { pk ->
            try {
                this.handleDataPacket(pk)
            } catch (e: Exception) {
                log.error(
                    "An error occurred whilst handling {} for {}", pk.javaClass.simpleName,
                    socketAddress.toString(), e
                )
            }
        }

        this.address = socketAddress as InetSocketAddress?
        log.debug("creating session {}", peer.socketAddress.toString())
        val cfg = StateMachineConfig<SessionState, SessionState>()

        cfg.configure(SessionState.Start)
            .onExit(Action { this.onSessionStartSuccess() })
            .permit(SessionState.Login, SessionState.Login)

        cfg.configure(SessionState.Login).onEntry(Action {
            this.packetHandler = (
                    LoginHandler(
                        this
                    ) { info: PlayerInfo? ->
                        this.info = info
                    })
        })
            .onExit(Action { this.onServerLoginSuccess() })
            .permitIf(
                SessionState.Encryption, SessionState.Encryption
            ) { Server.instance.enabledNetworkEncryption }
            .permit(SessionState.ResourcePack, SessionState.ResourcePack)

        cfg.configure(SessionState.Encryption)
            .onEntry(Action {
                log.debug("Player {} enter ENCRYPTION stage", peer.socketAddress.toString())
                this.packetHandler = (HandshakePacketHandler(this))
            })
            .permit(SessionState.ResourcePack, SessionState.ResourcePack)

        cfg.configure(SessionState.ResourcePack)
            .onEntry(Action {
                log.debug("Player {} enter RESOURCE_PACK stage", peer.socketAddress.toString())
                this.packetHandler = (ResourcePackHandler(this))
            })
            .permit(SessionState.PreSpawn, SessionState.PreSpawn)

        cfg.configure(SessionState.PreSpawn)
            .onEntry(Action {
                // now the main thread owns the session
                this.setNettyThreadOwned(false)

                log.debug("Creating player")

                val player = this.createPlayer()
                if (player == null) {
                    this.close("Failed to create player")
                    return@Action
                }
                this.onPlayerCreated(player)
                player.processLogin()
                this.packetHandler = (SpawnResponseHandler(this))
                // The reason why teleport player to their position is for gracefully client-side spawn,
                // although we need some hacks, It is definitely a fairly worthy trade.
                player.setImmobile(true) // TODO: HACK: fix client-side falling pre-spawn
            })
            .onExit(Action { this.onClientSpawned() })
            .permit(SessionState.InGame, SessionState.InGame)

        cfg.configure(SessionState.InGame)
            .onEntry(Action { this.packetHandler = (InGamePacketHandler(this)) })
            .onExit(Action { this.onServerDeath() })
            .permit(SessionState.Death, SessionState.Death)

        cfg.configure(SessionState.Death) //.onEntry(()->this.setPacketHandler(new DeathHandler()))
            .onExit(Action { this.onClientRespawn() })
            .permit(SessionState.InGame, SessionState.InGame)

        machine = StateMachine(SessionState.Start, cfg)
        this.packetHandler = (SessionStartHandler(this))
    }

    fun setNettyThreadOwned(immediatelyHandle: Boolean) {
        nettyThreadOwned.set(immediatelyHandle)
    }

    fun isNettyThreadOwned(): Boolean {
        return nettyThreadOwned.get()
    }

    fun setPacketConsumer(consumer: ((Packet) -> Unit)?) {
        this.consumer.set(consumer)
    }

    fun flush() {
        if (isDisconnected) {
            return
        }
        peer.flush()
    }

    fun sendPacket(packet: Packet) {
        if (isDisconnected) {
            return
        }
        val ev = PacketSendEvent(player, packet)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return
        }
        peer.sendPacket(this.subClientId, 0, packet)
        this.logOutbound(packet)
    }

    fun sendPlayStatus(status: org.chorus_oss.protocol.packets.PlayStatusPacket.Companion.Status, immediate: Boolean) {
        val pk = org.chorus_oss.protocol.packets.PlayStatusPacket(
            status = status
        )
        if (immediate) {
            this.sendPacketImmediately(pk)
        } else {
            this.sendPacket(pk)
        }
    }

    fun sendRawPacket(pid: Int, buf2: ByteBuf) {
        if (isDisconnected) {
            return
        }
        val bedrockPacketCodec = peer.channel.pipeline().get(
            BedrockPacketCodec::class.java
        )
        val buf1 = ByteBufAllocator.DEFAULT.ioBuffer(4)
        val msg = BedrockPacketWrapper(pid, this.subClientId, 0, null, null)
        bedrockPacketCodec.encodeHeader(buf1, msg)
        val compositeBuf = Unpooled.compositeBuffer()
        compositeBuf.addComponents(true, buf1, buf2)
        msg.packetBuffer = compositeBuf
        peer.sendRawPacket(msg)
    }

    fun sendPacketImmediately(packet: Packet) {
        if (isDisconnected) {
            return
        }

        val ev = PacketSendEvent(player, packet)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return
        }

        peer.sendPacketImmediately(this.subClientId, 0, packet)
        this.logOutbound(packet)
    }

    fun sendPacketSync(packet: Packet) {
        if (isDisconnected) {
            return
        }
        val ev = PacketSendEvent(player, packet)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return
        }
        peer.sendPacketSync(this.subClientId, 0, packet)
        this.logOutbound(packet)
    }

    fun sendNetworkSettingsPacket(pk: org.chorus_oss.protocol.packets.NetworkSettingsPacket) {
        val alloc = peer.channel.alloc()
        val buf1 = alloc.buffer(16)
        val header = alloc.ioBuffer(5)
        val msg = BedrockPacketWrapper(0, subClientId, 0, pk, null)
        try {
            val bedrockPacketCodec = peer.channel.pipeline().get(BedrockPacketCodec.NAME) as BedrockPacketCodec
            val packet = msg.packet
            msg.packetId = packet!!.id
            bedrockPacketCodec.encodeHeader(buf1, msg)
            buf1.writeBytes(
                Buffer().apply {
                    requireNotNull(PacketRegistry[packet]) { "PacketCodec not registered for $packet" }.serialize(
                        packet,
                        this
                    )
                }.readByteArray()
            )

            val batch: BedrockBatchWrapper = BedrockBatchWrapper.newInstance()
            val buf2 = alloc.compositeDirectBuffer(2)
            ByteBufVarInt.writeUnsignedInt(header, buf1.readableBytes())
            buf2.addComponent(true, header)
            buf2.addComponent(true, buf1)
            batch.setCompressed(buf2)
            peer.channel.writeAndFlush(batch)
        } catch (t: Throwable) {
            log.error("Error send", t)
        } finally {
            msg.release()
        }
    }

    fun flushSendBuffer() {
        if (isDisconnected) {
            return
        }
        peer.flushSendQueue()
    }

    fun setCompression(algorithm: PacketCompressionAlgorithm) {
        check(!isSubClient) { "The compression algorithm can only be set by the primary session" }
        peer.setCompression(algorithm)
    }

    fun enableEncryption(key: AES.CTR.Key) {
        check(!isSubClient) { "Encryption can only be enabled by the primary session" }
        peer.enableEncryption(key)
    }

    fun onPacket(wrapper: BedrockPacketWrapper) {
        val packet = wrapper.packet!!
        this.logInbound(packet)

        val ev = DataPacketDecodeEvent(player, wrapper)
        Server.instance.pluginManager.callEvent(ev)

        val predictMaxBuffer = when (ev.packetId) {
            LoginPacket.id -> 10000000
            PlayerSkinPacket.id -> 5000000
            else -> 25000
        }
        if (ev.packetBuffer.length > predictMaxBuffer) {
            ev.cancelled = true
        }

        if (ev.cancelled) return

        if (nettyThreadOwned.get()) {
            val c = consumer.get()
            c?.invoke(packet)
        } else {
            inbound.add(packet)
        }
    }

    protected fun logOutbound(packet: Packet) {
        if (Server.instance.isLoggedPacket(packet)) {
            log.info(
                "Outbound {}({}): {}",
                socketAddress, this.subClientId, packet
            )
        } else if (log.isTraceEnabled && !Server.instance.isIgnoredPacket(packet)) {
            log.trace(
                "Outbound {}({}): {}",
                socketAddress, this.subClientId, packet
            )
        }
    }

    protected fun logInbound(packet: Packet) {
        if (Server.instance.isLoggedPacket(packet)) {
            log.info(
                "Inbound {}({}): {}",
                socketAddress, this.subClientId, packet
            )
        } else if (log.isTraceEnabled && !Server.instance.isIgnoredPacket(packet)) {
            log.trace(
                "Inbound {}({}): {}",
                socketAddress, this.subClientId, packet
            )
        }
    }

    val socketAddress: SocketAddress
        get() = peer.socketAddress

    val isSubClient: Boolean
        get() = this.subClientId != 0

    val isDisconnected: Boolean
        get() = closed.get()

    /**
     * Close Network Session.
     *
     * @param reason the reason,when it is not null,will send a DisconnectPacket to client
     */
    @ApiStatus.Internal
    fun close(reason: String?) {
        if (closed.get()) {
            return
        }

        //when a player haven't login,it only hold a BedrockSession,and Player Instance is null
        if (reason != null) {
            val packet = org.chorus_oss.protocol.packets.DisconnectPacket(
                reason = DisconnectFailReason.Unknown,
                hideDisconnectionScreen = false,
                message = reason,
                filteredMessage = reason,
            )
            this.sendPacketImmediately(packet)
        }

        Server.instance.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, {
            if (isSubClient) {
                // FIXME: Do sub-clients send a server-bound DisconnectPacket?
            } else {
                // Primary sub-client controls the connection
                peer.close()
            }
        }, 5)
    }

    /**
     * Player disconnection process
     *
     *
     * 1.BedrockSession#close -> channel#disconnect -> channelInactive-> BedrockPeer#onClose -> all BedrockSession#onClose -> tickFuture#cancel -> free
     *
     *
     * 2.onRakNetDisconnect -> channel#disconnect -> channelInactive-> BedrockPeer#onClose -> all BedrockSession#onClose -> tickFuture#cancel -> free
     *
     *
     * 3.Player#close -> BedrockSession#close
     */
    fun onClose() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
        val player = this.player
        player?.close(BedrockDisconnectReasons.DISCONNECTED)
        Server.instance.network.onSessionDisconnect(address)
        peer.removeSession(this)
    }

    val isConnected: Boolean
        get() = !closed.get()

    val ping: Long
        get() {
            if (isDisconnected) {
                return -1L
            }
            return peer.ping
        }

    fun onPlayerCreated(player: Player) {
        this.player = player
        Server.instance.onPlayerLogin(address, player)
    }

    fun notifyTerrainReady() {
        log.debug("Sending spawn notification, waiting for spawn response")
        val state = machine.state
        check(state == SessionState.PreSpawn) { "attempt to notifyTerrainReady when the state is " + state.name }
        player!!.doFirstSpawn()
    }

    fun onSessionStartSuccess() {
        log.debug("Waiting for login packet")
    }

    private fun createPlayer(): Player? {
        try {
            val event = PlayerCreationEvent(Player::class.java)
            Server.instance.pluginManager.callEvent(event)
            val constructor = event.playerClass.getConstructor(
                BedrockSession::class.java,
                PlayerInfo::class.java
            )
            return constructor.newInstance(this, this.info)
        } catch (e: Exception) {
            log.error("Failed to create player", e)
        }
        return null
    }

    private fun onServerLoginSuccess() {
        log.debug("Login completed")
        this.sendPlayStatus(org.chorus_oss.protocol.packets.PlayStatusPacket.Companion.Status.LoginSuccess, false)
    }

    private fun onClientSpawned() {
        log.debug("Received spawn response, entering in-game phase")
        player!!.setImmobile(false) //TODO: HACK: we set this during the spawn sequence to prevent the client sending junk movements
    }

    protected fun onServerDeath() {
    }

    protected fun onClientRespawn() {
    }

    fun handleDataPacket(packet: Packet) {
        val ev = PacketReceiveEvent(player, packet)
        Server.instance.pluginManager.callEvent(ev)

        if (ev.cancelled) return

        if (this.packetHandler == null) return

        val inGamePacketHandler = packetHandler
        if (inGamePacketHandler is InGamePacketHandler) {
            inGamePacketHandler.managerHandle(packet)
        } else {
            this.packetHandler?.handle(packet)
        }
    }

    fun tick() {
        val c = consumer.get()
        if (c != null) {
            while (true) {
                val packet = inbound.poll() ?: break
                c.invoke(packet)
            }
        } else {
            inbound.clear()
        }
    }

    val addressString: String
        get() = address!!.address.hostAddress

    fun syncAvailableCommands() {
        val data: MutableMap<String, CommandDataVersions> = HashMap()
        var count = 0
        val commands = Server.instance.commandMap.commands
        synchronized(commands) {
            for (command in commands.values) {
                if (!command.testPermissionSilent(player!!) || !command.isRegistered || command.isServerSideOnly) {
                    continue
                }
                ++count
                val data0 = command.generateCustomCommandData(player!!)
                data[command.name] = data0!!
            }
        }

        if (count > 0) {
            // TODO: structure checking
            val pk = org.chorus_oss.protocol.packets.AvailableCommandsPacket(data)
            this.sendPacket(pk)
        }
    }

    fun syncCraftingData() {
        this.sendRawPacket(CraftingDataPacket.id, Registries.RECIPE.craftingPacket)
    }

    fun syncCreativeContent() {
        val pk = org.chorus_oss.protocol.packets.CreativeContentPacket(
            groups = CreativeItemRegistry.creativeGroups,
            items = CreativeItemRegistry.creativeItemData.map(org.chorus_oss.protocol.types.creative.CreativeItem::invoke)
        )
        this.sendPacket(pk)
    }

    fun syncInventory() {
        val player = player
        if (player != null) {
            player.inventory.sendHeldItem(player)
            player.inventory.sendContents(player)
            player.inventory.sendArmorContents(player)
            player.cursorInventory.sendContents(player)
            player.offhandInventory.sendContents(player)
            player.enderChestInventory.sendContents(player)
        }
    }

    fun setEnableClientCommand(enable: Boolean) {
        val pk = org.chorus_oss.protocol.packets.SetCommandsEnabledPacket(
            enabled = enable
        )
        this.sendPacket(pk)
        if (enable) {
            this.syncAvailableCommands()
        }
    }

    fun setAuthenticated() {
        authenticated = true
    }

    val server: Server
        get() = Server.instance

    val packetManager: PacketManager?
        get() {
            val inGamePacketHandler = this.packetHandler
            return if (inGamePacketHandler != null && inGamePacketHandler is InGamePacketHandler) {
                inGamePacketHandler.manager
            } else {
                null
            }
        }
}
