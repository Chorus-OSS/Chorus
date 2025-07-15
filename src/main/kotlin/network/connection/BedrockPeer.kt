package org.chorus_oss.chorus.network.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.DecoderException
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.ScheduledFuture
import io.netty.util.internal.PlatformDependent
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper
import org.chorus_oss.chorus.network.connection.netty.codec.FrameIdCodec
import org.chorus_oss.chorus.network.connection.netty.codec.batch.BedrockBatchDecoder
import org.chorus_oss.chorus.network.connection.netty.codec.compression.CompressionCodec
import org.chorus_oss.chorus.network.connection.netty.codec.compression.CompressionStrategy
import org.chorus_oss.chorus.network.connection.netty.codec.encryption.BedrockEncryptionDecoder
import org.chorus_oss.chorus.network.connection.netty.codec.encryption.BedrockEncryptionEncoder
import org.chorus_oss.chorus.network.connection.netty.initializer.BedrockChannelInitializer
import org.chorus_oss.chorus.network.connection.util.EncryptionUtils
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.chorus_oss.chorus.utils.Loggable
import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason
import org.cloudburstmc.netty.channel.raknet.RakServerChannel
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.netty.handler.codec.raknet.common.RakSessionCodec
import org.jetbrains.annotations.ApiStatus
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.SecretKey

/**
 * A Bedrock peer that represents a single network connection to the remote peer.
 * It can hold one or more [BedrockSession]s.
 */
class BedrockPeer(val channel: Channel, private val sessionFactory: BedrockSessionFactory) :
    ChannelInboundHandlerAdapter(), Loggable {
    private val sessions: MutableMap<Int, BedrockSession> = mutableMapOf()
    private val packetQueue: Queue<BedrockPacketWrapper> = PlatformDependent.newMpscQueue()
    private var tickFuture: ScheduledFuture<*>? = null
    private var closed: AtomicBoolean = AtomicBoolean()

    private fun onBedrockPacket(wrapper: BedrockPacketWrapper) {
        val targetId = wrapper.targetSubClientId
        val session = sessions.computeIfAbsent(
            targetId
        ) { sessionId -> this.onSessionCreated(sessionId)!! }
        session.onPacket(wrapper)
    }

    private fun onSessionCreated(sessionId: Int): BedrockSession? {
        return sessionFactory.createSession(this, sessionId)
    }

    private fun checkForClosed() {
        check(!closed.get()) { "Peer has been closed" }
    }

    fun removeSession(session: BedrockSession) {
        sessions.remove(session.subClientId, session as Any)
    }

    private fun onTick() {
        if (closed.get()) {
            return
        }

        this.flushSendQueue()
    }

    fun flushSendQueue() {
        if (!packetQueue.isEmpty()) {
            while (true) {
                val packet = packetQueue.poll() ?: break
                if (this.isConnected) {
                    channel.write(packet)
                }
            }
            channel.flush()
        }
    }

    private fun onRakNetDisconnect(reason: RakDisconnectReason) {
        val disconnectReason = BedrockDisconnectReasons.getReason(reason)
        for (session in sessions.values) {
            session.close(disconnectReason)
        }
    }

    private fun free() {
        for (wrapper in this.packetQueue) {
            ReferenceCountUtil.safeRelease(wrapper)
        }
    }

    /**
     * Send packet Asynchronously.
     *
     * @param senderClientId the sender client id
     * @param targetClientId the target client id
     * @param packet         the packet
     */
    fun sendPacket(senderClientId: Int, targetClientId: Int, packet: DataPacket?) {
        packetQueue.add(BedrockPacketWrapper(0, senderClientId, targetClientId, packet, null))
    }

    fun sendPacketSync(senderClientId: Int, targetClientId: Int, packet: DataPacket?) {
        channel.writeAndFlush(BedrockPacketWrapper(0, senderClientId, targetClientId, packet, null))
            .syncUninterruptibly()
    }

    /**
     * Send packet immediately Asynchronously.
     *
     * @param senderClientId the sender client id
     * @param targetClientId the target client id
     * @param packet         the packet
     */
    fun sendPacketImmediately(senderClientId: Int, targetClientId: Int, packet: DataPacket?) {
        channel.writeAndFlush(BedrockPacketWrapper(0, senderClientId, targetClientId, packet, null))
    }

    fun sendRawPacket(packet: BedrockPacketWrapper?) {
        channel.writeAndFlush(packet)
    }

    fun flush() {
        channel.flush()
    }

    fun enableEncryption(secretKey: SecretKey) {
        Objects.requireNonNull(secretKey, "secretKey")
        require(secretKey.algorithm == "AES") { "Invalid key algorithm" }
        // Check if the codecs exist in the pipeline
        check(
            !(channel.pipeline().get(BedrockEncryptionEncoder::class.java) != null ||
                    channel.pipeline().get(BedrockEncryptionDecoder::class.java) != null)
        ) { "Encryption is already enabled" }

        channel.pipeline().addAfter(
            FrameIdCodec.NAME, BedrockEncryptionEncoder.NAME,
            BedrockEncryptionEncoder(secretKey, EncryptionUtils.createCipher(true, secretKey))
        )
        channel.pipeline().addAfter(
            FrameIdCodec.NAME, BedrockEncryptionDecoder.NAME,
            BedrockEncryptionDecoder(secretKey, EncryptionUtils.createCipher(false, secretKey))
        )

        log.debug("Encryption enabled for {}", socketAddress)
    }

    fun setCompression(algorithm: PacketCompressionAlgorithm) {
        this.setCompression(
            BedrockChannelInitializer.getCompression(
                algorithm,
                rakVersion, false
            )
        )
    }

    private fun setCompression(strategy: CompressionStrategy) {
        val needsPrefix = org.chorus_oss.protocol.ProtocolInfo.VERSION >= 649 // TODO: do not hardcode

        val handler = channel.pipeline()[CompressionCodec.NAME]
        if (handler == null) {
            channel.pipeline().addBefore(
                BedrockBatchDecoder.NAME,
                CompressionCodec.NAME,
                CompressionCodec(strategy, needsPrefix)
            )
        } else {
            channel.pipeline().replace(
                CompressionCodec.NAME,
                CompressionCodec.NAME,
                CompressionCodec(strategy, needsPrefix)
            )
        }
    }

    val compressionStrategy: CompressionStrategy?
        get() {
            val handler = channel.pipeline()[CompressionCodec.NAME] as? CompressionCodec ?: return null
            return handler.strategy
        }

    @ApiStatus.Internal
    fun close() {
        channel.disconnect()
    }

    private fun onClose() {
        if (channel.isOpen) {
            log.warn("Tried to close peer, but channel is open!", Throwable())
            return
        }

        for (session in sessions.values) {
            try {
                session.onClose()
            } catch (e: Exception) {
                log.error("Exception whilst closing session", e)
            }
        }

        if (!closed.compareAndSet(false, true)) {
            return
        }

        if (this.tickFuture != null) {
            tickFuture!!.cancel(false)
            this.tickFuture = null
        }

        this.free()
    }

    val isConnected: Boolean
        get() = !closed.get() && channel.isOpen

    val isConnecting: Boolean
        get() = !channel.isActive && !closed.get()

    val socketAddress: SocketAddress
        get() = channel.remoteAddress()

    val rakVersion: Int
        get() = channel.config().getOption(RakChannelOption.RAK_PROTOCOL_VERSION)

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        sessions[0] = sessionFactory.createSession(this, 0)!!
        this.tickFuture =
            channel.eventLoop().scheduleAtFixedRate({ this.onTick() }, 10, 10, TimeUnit.MILLISECONDS)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        this.onClose()
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        try {
            if (msg is BedrockPacketWrapper) {
                this.onBedrockPacket(msg)
            } else {
                throw DecoderException("Unexpected message type: " + msg.javaClass.name)
            }
        } finally {
            ReferenceCountUtil.release(msg)
        }
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is RakDisconnectReason) {
            onRakNetDisconnect(evt)
        }
    }

    val ping: Long
        get() {
            val rakServerChannel = channel.parent() as RakServerChannel
            val childChannel = rakServerChannel.getChildChannel(socketAddress)
            val rakSessionCodec =
                childChannel.rakPipeline().get(RakSessionCodec::class.java)
            return rakSessionCodec.ping
        }

    companion object {
        const val NAME: String = "bedrock-peer"
    }
}
