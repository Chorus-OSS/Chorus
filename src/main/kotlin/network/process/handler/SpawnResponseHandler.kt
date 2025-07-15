package org.chorus_oss.chorus.network.process.handler

import kotlinx.io.bytestring.ByteString
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.getPacketCache
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.getPlayerPropertyCache
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.protocol.StartGamePacket
import org.chorus_oss.chorus.network.protocol.types.TrimData
import org.chorus_oss.chorus.registry.ItemRegistry
import org.chorus_oss.chorus.registry.ItemRuntimeIdRegistry
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.RequestChunkRadiusPacket
import org.chorus_oss.protocol.packets.SetLocalPlayerAsInitializedPacket
import org.chorus_oss.protocol.types.TrimMaterial
import org.chorus_oss.protocol.types.TrimPattern
import org.chorus_oss.protocol.types.item.ItemEntry
import kotlin.math.max
import kotlin.math.min

class SpawnResponseHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    init {
        val server: Server = Server.instance

        this.startGame()

        log.debug("Sending item components")
        val entries = mutableSetOf<ItemEntry>()

        for (data in ItemRuntimeIdRegistry.ITEM_DATA) {
            var tag = CompoundTag()

            if (ItemRegistry.itemComponents.containsCompound(data.identifier)) {
                val itemTag = ItemRegistry.itemComponents.getCompound(data.identifier)
                tag.putCompound("components", itemTag.getCompound("components"))
            } else if (Registries.ITEM.customItemDefinition.containsKey(data.identifier)) {
                tag = Registries.ITEM.customItemDefinition[data.identifier]!!.nbt
            }

            entries.add(
                ItemEntry(
                    data.identifier,
                    data.runtimeId.toShort(),
                    data.componentBased,
                    data.version,
                    org.chorus_oss.nbt.tags.CompoundTag(tag)
                )
            )
        }

        player!!.sendPacket(
            org.chorus_oss.protocol.packets.ItemRegistryPacket(
                items = entries.toList()
            )
        )

        log.debug("Sending actor identifiers")
        player.sendPacket(
            org.chorus_oss.protocol.packets.AvailableActorIdentifiersPacket(
                ByteString(Registries.ENTITY.tag)
            )
        )

        // 注册实体属性
        // Register entity attributes

        log.debug("Sending actor properties")
        for (pk in getPacketCache()) {
            player.sendPacket(pk)
        }

        log.debug("Sending biome definitions")
        player.sendPacket(Registries.BIOME.biomeDefinitionListPacket)

        log.debug("Sending attributes")
        player.syncAttributes()

        log.debug("Sending available commands")
        this.session.syncAvailableCommands()

        // 发送玩家权限列表
        // Send player permission list

        log.debug("Sending abilities")
        val col = setOf(player)
        server.onlinePlayers.values.forEach { p: Player ->
            if (p !== player) {
                p.adventureSettings.sendAbilities(col)
                p.adventureSettings.updateAdventureSettings()
            }
        }

        log.debug("Sending effects")
        player.sendPotionEffects(player)

        log.debug("Sending actor metadata")
        player.sendData(player)

        log.debug("Sending inventory")
        this.session.syncInventory()

        log.debug("Sending creative content")
        this.session.syncCreativeContent()

        log.debug("Sending trim data")
        val trimDataPacket = org.chorus_oss.protocol.packets.TrimDataPacket(
            patterns = TrimData.trimPatterns.map(TrimPattern::invoke),
            materials = TrimData.trimMaterials.map(TrimMaterial::invoke),
        )
        this.session.sendPacket(trimDataPacket)

        player.setNameTagVisible(true)
        player.setNameTagAlwaysVisible(true)
        player.setCanClimb(true)
        player.sendMovementSpeed(player.movementSpeed)

        log.debug("Sending player list")
        server.addOnlinePlayer(player)
        server.onPlayerCompleteLoginSequence(player)

        if (player.isOp || player.hasPermission("nukkit.textcolor")) {
            player.removeFormat = false
        }
    }

    private fun startGame() {
        val server: Server = Server.instance
        val startPk = StartGamePacket()

        startPk.entityUniqueId = player!!.getRuntimeID()
        startPk.entityRuntimeId = player.getRuntimeID()
        startPk.playerGamemode = Player.toNetworkGamemode(player.gamemode)

        startPk.x = player.position.x.toFloat()
        startPk.y =
            (if (player.isOnGround()) player.position.y + player.getEyeHeight() else player.position.y).toFloat() //防止在地上生成容易陷进地里
        startPk.z = player.position.z.toFloat()
        startPk.yaw = player.rotation.yaw.toFloat()
        startPk.pitch = player.rotation.pitch.toFloat()
        startPk.seed = -1L
        startPk.dimension = (player.level!!.dimension and 0xff).toByte()
        startPk.worldGamemode = Player.toNetworkGamemode(server.defaultGamemode)
        startPk.difficulty = server.getDifficulty()
        val spawn = player.safeSpawn
        startPk.spawnX = spawn.floorX
        startPk.spawnY = spawn.floorY
        startPk.spawnZ = spawn.floorZ
        startPk.hasAchievementsDisabled = true
        startPk.dayCycleStopTime = -1
        startPk.rainLevel = 0f
        startPk.lightningLevel = 0f
        startPk.commandsEnabled = player.isEnableClientCommand()
        startPk.gameRules = player.level!!.gameRules
        startPk.levelId = ""
        startPk.worldName = server.subMotd
        startPk.generator = ((player.level!!.dimension + 1) and 0xff).toByte()
            .toInt() //0 旧世界 Old world, 1 主世界 Main world, 2 下界 Nether, 3 末地 End
        startPk.serverAuthoritativeMovement = server.getServerAuthoritativeMovement()
        startPk.isInventoryServerAuthoritative = true //enable item stack request packet
        startPk.blockNetworkIdsHashed = true //enable blockhash
        // 写入自定义方块数据
        // Write custom block data
        startPk.blockProperties.addAll(Registries.BLOCK.customBlockDefinitionList)
        startPk.playerPropertyData = getPlayerPropertyCache()
        player.dataPacketImmediately(startPk)
    }

    override fun handle(pk: MigrationPacket<*>) {
        val packet = pk.packet
        when (packet) {
            is RequestChunkRadiusPacket -> handleRadius(packet)
            is SetLocalPlayerAsInitializedPacket -> handleInitialized(packet)
        }
    }

    fun handleRadius(packet: RequestChunkRadiusPacket) {
        player!!.viewDistance = max(2.0, min(packet.chunkRadius.toDouble(), player.viewDistance.toDouble())).toInt()
    }

    fun handleInitialized(packet: SetLocalPlayerAsInitializedPacket) {
        log.debug(
            "receive SetLocalPlayerAsInitializedPacket for {}",
            player?.playerInfo?.username
        )
        player?.onPlayerLocallyInitialized()
    }

    companion object : Loggable
}
