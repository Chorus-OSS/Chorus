package org.chorus_oss.chorus.network.process.handler

import kotlinx.io.bytestring.ByteString
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.getPacketCache
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.getPlayerPropertyCache
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.protocol.types.TrimData
import org.chorus_oss.chorus.registry.ItemRegistry
import org.chorus_oss.chorus.registry.ItemRuntimeIdRegistry
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.*
import org.chorus_oss.protocol.types.*
import org.chorus_oss.protocol.types.item.ItemEntry
import kotlin.math.max
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
            ItemRegistryPacket(
                items = entries.toList()
            )
        )

        log.debug("Sending actor identifiers")
        player.sendPacket(
            AvailableActorIdentifiersPacket(
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
        val trimDataPacket = TrimDataPacket(
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

    @OptIn(ExperimentalUuidApi::class)
    private fun startGame() {
        requireNotNull(player)

        val server: Server = Server.instance
        val packet = StartGamePacket(
            entityUniqueID = player.getUniqueID(),
            entityRuntimeID = player.getRuntimeID().toULong(),
            playerGameMode = Player.toNetworkGamemode(player.gamemode),
            playerPosition = Vector3f(
                player.position.x.toFloat(),
                (if (player.isOnGround()) player.position.y + player.getEyeHeight() else player.position.y).toFloat(),
                player.position.z.toFloat()
            ),
            pitch = player.rotation.pitch.toFloat(),
            yaw = player.rotation.yaw.toFloat(),
            worldSeed = -1,
            spawnBiomeType = SpawnBiomeType.Default,
            userDefinedBiomeName = "plains",
            dimension = (player.level!!.dimension and 0xff),
            generator = ((player.level!!.dimension + 1) and 0xff), // 0: Legacy, 1: Overworld, 2: Nether, 3: The End
            worldGameMode = Player.toNetworkGamemode(server.defaultGamemode),
            hardcore = false,
            difficulty = server.getDifficulty(),
            worldSpawn = BlockPos(player.safeSpawn),
            achievementsDisabled = true,
            editorWorldType = EditorWorldType.NotEditor,
            createdInEditor = false,
            exportedFromEditor = false,
            dayCycleLockTime = -1,
            educationEditionOffer = 0,
            educationFeaturesEnabled = false,
            educationProductID = "",
            rainLevel = 0f,
            lightningLevel = 0f,
            confirmedPlatformLockedContent = false,
            multiPlayerGame = true,
            lanBroadcastEnabled = true,
            xblBroadcastMode = BroadcastMode.Public,
            platformBroadcastMode = BroadcastMode.Public,
            commandsEnabled = player.isEnableClientCommand(),
            texturePackRequired = false,
            gameRules = player.level!!.gameRules.getGameRules().entries.map { it.toPair() }.map { GameRule(it) },
            experiments = listOf(
                ExperimentData("data_driven_items", true),
                ExperimentData("data_driven_biomes", true),
                ExperimentData("upcoming_creator_features", true),
                ExperimentData("gametest", true),
                ExperimentData("experimental_molang_features", true),
                ExperimentData("cameras", true),
            ),
            experimentsPreviouslyToggled = true,
            bonusChestEnabled = false,
            startWithMapEnabled = false,
            playerPermissions = 1,
            serverChunkTickRadius = 4,
            hasLockedBehaviourPack = false,
            hasLockedTexturePack = false,
            fromLockedWorldTemplate = false,
            msaGamerTagsOnly = false,
            fromWorldTemplate = false,
            worldTemplateSettingsLocked = false,
            onlySpawnV1Villagers = false,
            personaDisabled = false,
            customSkinsDisabled = false,
            emoteChatMuted = false,
            baseGameVersion = "*",
            limitedWorldWidth = 16,
            limitedWorldDepth = 16,
            newNether = false,
            educationSharedResourceUriResource = EduSharedUriResource("", ""),
            forceExperimentalGameplay = null,
            chatRestrictionLevel = ChatRestrictionLevel.None,
            disablePlayerInteractions = false,
            serverID = "",
            worldID = "",
            scenarioID = "",
            ownerID = "",
            levelID = "",
            worldName = server.subMotd,
            templateContentIdentity = "",
            trial = false,
            playerMovementSettings = PlayerMovementSettings(
                rewindHistorySize = 0,
                serverAuthoritativeBlockBreaking = server.getServerAuthoritativeMovement() > 0
            ),
            tick = 0,
            enchantmentSeed = 0,
            blocks = Registries.BLOCK.customBlockDefinitionList.map {
                BlocksEntry(
                    name = it.identifier,
                    properties = org.chorus_oss.nbt.tags.CompoundTag(it.nbt)
                )
            },
            multiPlayerCorrelationID = "",
            serverAuthoritativeInventory = true,
            gameVersion = ProtocolInfo.VERSION,
            propertyData = org.chorus_oss.nbt.tags.CompoundTag(getPlayerPropertyCache()),
            serverBlockStateChecksum = 0u,
            clientSideGeneration = false,
            worldTemplateID = Uuid.fromLongs(0, 0),
            useBlockNetworkIDHashes = true,
            serverAuthoritativeSound = false,
        )
        player.sendPacketImmediately(packet)
    }

    override fun handle(packet: Packet) {
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
