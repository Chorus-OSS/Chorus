package org.chorus_oss.chorus.network.protocol


import org.chorus_oss.chorus.block.customblock.CustomBlockDefinition
import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.chorus.nbt.NBTIO.write
import org.chorus_oss.chorus.nbt.NBTIO.writeNetwork
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.utils.Loggable
import java.io.IOException
import java.nio.ByteOrder
import java.util.*

class StartGamePacket : DataPacket() {
    var entityUniqueId: Long = 0
    var entityRuntimeId: Long = 0
    var playerGamemode: Int = 0
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var yaw: Float = 0f
    var pitch: Float = 0f
    var seed: Long = 0
    var dimension: Byte = 0
    var generator: Int = 1
    var worldGamemode: Int = 0
    var isHardcore: Boolean = false
    var difficulty: Int = 0
    var spawnX: Int = 0
    var spawnY: Int = 0
    var spawnZ: Int = 0
    var hasAchievementsDisabled: Boolean = true
    var worldEditor: Boolean = false
    var dayCycleStopTime: Int = -1 //-1 = not stopped, any positive value = stopped at that time
    var eduEditionOffer: Int = 0
    var hasEduFeaturesEnabled: Boolean = false
    var rainLevel: Float = 0f
    var lightningLevel: Float = 0f
    var hasConfirmedPlatformLockedContent: Boolean = false
    var multiplayerGame: Boolean = true
    var broadcastToLAN: Boolean = true
    var xblBroadcastIntent: Int = GAME_PUBLISH_SETTING_PUBLIC
    var platformBroadcastIntent: Int = GAME_PUBLISH_SETTING_PUBLIC
    var commandsEnabled: Boolean = false
    var isTexturePacksRequired: Boolean = false
    var gameRules: GameRules? = null
    var bonusChest: Boolean = false
    var hasStartWithMapEnabled: Boolean = false
    var permissionLevel: Int = 1
    var serverChunkTickRange: Int = 4
    var hasLockedBehaviorPack: Boolean = false
    var hasLockedResourcePack: Boolean = false
    var isFromLockedWorldTemplate: Boolean = false
    var isUsingMsaGamertagsOnly: Boolean = false
    var isFromWorldTemplate: Boolean = false
    var isWorldTemplateOptionLocked: Boolean = false
    var isOnlySpawningV1Villagers: Boolean = false
    var vanillaVersion: String = ProtocolInfo.GAME_VERSION_NET

    // HACK: For now we can specify this version, since the new chunk changes are not relevant for our Anvil format.
    // However, it could be that Microsoft will prevent this in a new update.
    var playerPropertyData: CompoundTag = CompoundTag()
    var levelId: String = "" // base64 string, usually the same as world folder name in vanilla
    var worldName: String? = null
    var premiumWorldTemplateId: String = ""
    var isTrial: Boolean = false
    var isMovementServerAuthoritative: Boolean = false
    var serverAuthoritativeMovement: Int? = null
    var isInventoryServerAuthoritative: Boolean = false
    var currentTick: Long = 0
    var enchantmentSeed: Int = 0
    val blockProperties: MutableList<CustomBlockDefinition> = mutableListOf()
    var multiplayerCorrelationId: String = ""
    var isDisablingPersonas: Boolean = false
    var isDisablingCustomSkins: Boolean = false
    var clientSideGenerationEnabled: Boolean = false

    var emoteChatMuted: Boolean = false

    /**
     * Whether block runtime IDs should be replaced by 32-bit integer hashes of the NBT block state.
     * Unlike runtime IDs, this hashes should be persistent across versions and should make support for data-driven/custom blocks easier.
     */
    var blockNetworkIdsHashed: Boolean = false

    var createdInEditor: Boolean = false


    var exportedFromEditor: Boolean = false
    var chatRestrictionLevel: Byte = 0
    var disablePlayerInteractions: Boolean = false

    var isSoundsServerAuthoritative: Boolean = false

    val serverId = ""
    val worldId = ""
    val scenarioId = ""
    val ownerId = ""

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeActorUniqueID(this.entityUniqueId)
        byteBuf.writeActorRuntimeID(this.entityRuntimeId)
        byteBuf.writeVarInt(this.playerGamemode)
        byteBuf.writeVector3f(this.x, this.y, this.z)
        byteBuf.writeFloatLE(this.yaw)
        byteBuf.writeFloatLE(this.pitch)
        writeLevelSettings(byteBuf)
        byteBuf.writeString(this.levelId)
        byteBuf.writeString(worldName!!)
        byteBuf.writeString(this.premiumWorldTemplateId)
        byteBuf.writeBoolean(this.isTrial)
        byteBuf.writeVarInt(0) // RewindHistorySize
        if (this.serverAuthoritativeMovement != null) {
            byteBuf.writeBoolean(serverAuthoritativeMovement!! > 0) // isServerAuthoritativeBlockBreaking
        } else { //兼容nkx旧插件
            byteBuf.writeBoolean(this.isMovementServerAuthoritative) // isServerAuthoritativeBlockBreaking
        }
        byteBuf.writeLongLE(this.currentTick)
        byteBuf.writeVarInt(this.enchantmentSeed)

        // Custom blocks
        byteBuf.writeUnsignedVarInt(blockProperties.size)
        try {
            for (customBlockDefinition in this.blockProperties) {
                byteBuf.writeString(customBlockDefinition.identifier)
                byteBuf.writeBytes(write(customBlockDefinition.nbt, ByteOrder.LITTLE_ENDIAN, true))
            }
        } catch (e: IOException) {
            log.error("Error while encoding NBT data of BlockPropertyData", e)
        }

        byteBuf.writeString(this.multiplayerCorrelationId)
        byteBuf.writeBoolean(this.isInventoryServerAuthoritative)
        byteBuf.writeString(vanillaVersion) // Server Engine
        try {
            byteBuf.writeBytes(writeNetwork(playerPropertyData)) // playerPropertyData
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        byteBuf.writeLongLE(0) // blockRegistryChecksum
        byteBuf.writeUUID(UUID(0, 0)) // worldTemplateId
        byteBuf.writeBoolean(this.clientSideGenerationEnabled)
        byteBuf.writeBoolean(this.blockNetworkIdsHashed) // blockIdsAreHashed
        byteBuf.writeBoolean(this.isSoundsServerAuthoritative) // serverAuthSounds
    }

    private fun writeLevelSettings(byteBuf: HandleByteBuf) {
        /* Level settings start */
        byteBuf.writeLongLE(this.seed)
        byteBuf.writeShortLE(0x00) // SpawnBiomeType - Default
        byteBuf.writeString("plains") // UserDefinedBiomeName
        byteBuf.writeVarInt(dimension.toInt())
        byteBuf.writeVarInt(this.generator)
        byteBuf.writeVarInt(this.worldGamemode)
        byteBuf.writeBoolean(this.isHardcore)
        byteBuf.writeVarInt(this.difficulty)
        byteBuf.writeBlockVector3(this.spawnX, this.spawnY, this.spawnZ)
        byteBuf.writeBoolean(this.hasAchievementsDisabled)
        byteBuf.writeBoolean(this.worldEditor)
        byteBuf.writeBoolean(this.createdInEditor)
        byteBuf.writeBoolean(this.exportedFromEditor)
        byteBuf.writeVarInt(this.dayCycleStopTime)
        byteBuf.writeVarInt(this.eduEditionOffer)
        byteBuf.writeBoolean(this.hasEduFeaturesEnabled)
        byteBuf.writeString("") // Education Edition Product ID
        byteBuf.writeFloatLE(this.rainLevel)
        byteBuf.writeFloatLE(this.lightningLevel)
        byteBuf.writeBoolean(this.hasConfirmedPlatformLockedContent)
        byteBuf.writeBoolean(this.multiplayerGame)
        byteBuf.writeBoolean(this.broadcastToLAN)
        byteBuf.writeVarInt(this.xblBroadcastIntent)
        byteBuf.writeVarInt(this.platformBroadcastIntent)
        byteBuf.writeBoolean(this.commandsEnabled)
        byteBuf.writeBoolean(this.isTexturePacksRequired)
        byteBuf.writeGameRules(gameRules!!)

        byteBuf.writeIntLE(6) // Experiment count
        run {
            byteBuf.writeString("data_driven_items")
            byteBuf.writeBoolean(true)
            byteBuf.writeString("data_driven_biomes")
            byteBuf.writeBoolean(true)
            byteBuf.writeString("upcoming_creator_features")
            byteBuf.writeBoolean(true)
            byteBuf.writeString("gametest")
            byteBuf.writeBoolean(true)
            byteBuf.writeString("experimental_molang_features")
            byteBuf.writeBoolean(true)
            byteBuf.writeString("cameras")
            byteBuf.writeBoolean(true)
        }
        byteBuf.writeBoolean(true) // Were experiments previously toggled

        byteBuf.writeBoolean(this.bonusChest)
        byteBuf.writeBoolean(this.hasStartWithMapEnabled)
        byteBuf.writeVarInt(this.permissionLevel)
        byteBuf.writeIntLE(this.serverChunkTickRange)
        byteBuf.writeBoolean(this.hasLockedBehaviorPack)
        byteBuf.writeBoolean(this.hasLockedResourcePack)
        byteBuf.writeBoolean(this.isFromLockedWorldTemplate)
        byteBuf.writeBoolean(this.isUsingMsaGamertagsOnly)
        byteBuf.writeBoolean(this.isFromWorldTemplate)
        byteBuf.writeBoolean(this.isWorldTemplateOptionLocked)
        byteBuf.writeBoolean(this.isOnlySpawningV1Villagers)
        byteBuf.writeBoolean(this.isDisablingPersonas)
        byteBuf.writeBoolean(this.isDisablingCustomSkins)
        byteBuf.writeBoolean(this.emoteChatMuted)
        byteBuf.writeString("*") // vanillaVersion
        byteBuf.writeIntLE(16) // Limited world width
        byteBuf.writeIntLE(16) // Limited world height
        byteBuf.writeBoolean(false) // Nether type
        byteBuf.writeString("") // EduSharedUriResource buttonName
        byteBuf.writeString("") // EduSharedUriResource linkUri
        byteBuf.writeBoolean(false) // force Experimental Gameplay (exclusive to debug clients)
        byteBuf.writeByte(chatRestrictionLevel.toInt())
        byteBuf.writeBoolean(this.disablePlayerInteractions)
        byteBuf.writeString(serverId)
        byteBuf.writeString(worldId)
        byteBuf.writeString(scenarioId)
        byteBuf.writeString(ownerId)
        /* Level settings end */
    }

    override fun pid(): Int {
        return ProtocolInfo.START_GAME_PACKET
    }

    companion object : Loggable {
        const val GAME_PUBLISH_SETTING_NO_MULTI_PLAY: Int = 0
        const val GAME_PUBLISH_SETTING_INVITE_ONLY: Int = 1
        const val GAME_PUBLISH_SETTING_FRIENDS_ONLY: Int = 2
        const val GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS: Int = 3
        const val GAME_PUBLISH_SETTING_PUBLIC: Int = 4
    }
}
