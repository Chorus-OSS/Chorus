package org.chorus_oss.chorus.level.format.leveldb

import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.protocol.types.GameType
import org.chorus_oss.chorus.utils.SemVersion

class LevelDat {
    var biomeOverride: String = ""
    var centerMapsToOrigin: Boolean = false
    var confirmedPlatformLockedContent: Boolean = false
    var difficulty: Int = 1
    var flatWorldLayers: String = ""
    var forceGameType: Boolean = false
    var gameType: GameType = GameType.from(0)
    var generator: Int = 1
    var inventoryVersion: String = "1.20.60"
    var LANBroadcast: Boolean = true
    var LANBroadcastIntent: Boolean = true
    var lastPlayed: Long = 0L
    var name: String = "Bedrock level"
    var limitedWorldOriginPoint: BlockVector3 = BlockVector3(0, 64, 0)
    var minimumCompatibleClientVersion: SemVersion = SemVersion(
        1,
        20,
        50,
        0,
        0
    )
    var multiplayerGame: Boolean = true
    var multiplayerGameIntent: Boolean = false
    var netherScale: Int = 8
    var networkVersion: Int = org.chorus_oss.protocol.ProtocolInfo.VERSION
    var platform: Int = 2
    var platformBroadcastIntent: Int = 0
    var randomSeed: Long = 1811906518383890446L
    var spawnV1Villagers: Boolean = false

    /**
     * The overworld default spawn point
     */
    var spawnPoint: BlockVector3 = BlockVector3(0, 70, 0)
    var storageVersion: Int = 10
    var time: Long = 0L
    var worldVersion: Int = 1
    var XBLBroadcastIntent: Int = 0
    var abilities: Abilities = Abilities()
    var baseGameVersion: String = "*"
    var bonusChestEnabled: Boolean = false
    var bonusChestSpawned: Boolean = false
    var cheatsEnabled: Boolean = false
    var commandsEnabled: Boolean = true
    var gameRules: GameRules = GameRules.default
    var currentTick: Long = 0L
    var daylightCycle: Int = 0
    var editorWorldType: Int = 0
    var eduOffer: Int = 0
    var educationFeaturesEnabled: Boolean = false
    var experiments: Experiments = Experiments()
    var hasBeenLoadedInCreative: Boolean = true
    var hasLockedBehaviorPack: Boolean = false
    var hasLockedResourcePack: Boolean = false
    var immutableWorld: Boolean = false
    var isCreatedInEditor: Boolean = false
    var isExportedFromEditor: Boolean = false
    var isFromLockedTemplate: Boolean = false
    var isFromWorldTemplate: Boolean = false
    var isRandomSeedAllowed: Boolean = false
    var isSingleUseWorld: Boolean = false
    var isWorldTemplateOptionLocked: Boolean = false
    var lastOpenedWithVersion: SemVersion = ProtocolInfo.GAME_VERSION
    var lightningLevel: Float = 0.0f
    var lightningTime: Int = 0 //thunderTime
    var limitedWorldDepth: Int = 16
    var limitedWorldWidth: Int = 16
    var permissionsLevel: Int = 0
    var playerPermissionsLevel: Int = 1
    var playersSleepingPercentage: Int = 100
    var prid: String = ""
    var rainLevel: Float = 0.0f
    var rainTime: Int = 0 //rainTime
    var randomTickSpeed: Int = 1
    var recipesUnlock: Boolean = false
    var requiresCopiedPackRemovalCheck: Boolean = false
    var serverChunkTickRange: Int = 4
    var spawnMobs: Boolean = true
    var startWithMapEnabled: Boolean = false
    var texturePacksRequired: Boolean = false
    var useMsaGamertagsOnly: Boolean = true
    var worldStartCount: Long = 0L
    var worldPolicies: WorldPolicies = WorldPolicies()

    class Abilities {
        var attackMobs: Boolean = true
        var attackPlayers: Boolean = true
        var build: Boolean = true
        var doorsAndSwitches: Boolean = true
        var flySpeed: Float = 0.05f
        var flying: Boolean = false
        var instaBuild: Boolean = false
        var invulnerable: Boolean = false
        var lightning: Boolean = false
        var mayFly: Boolean = false
        var mine: Boolean = true
        var op: Boolean = false
        var openContainers: Boolean = true
        var teleport: Boolean = false
        var walkSpeed: Float = 0.1f
    }

    class Experiments {
        var experimentsEverUsed: Boolean = false
        var savedWithToggledExperiments: Boolean = false
        var cameras: Boolean = false
        var dataDrivenBiomes: Boolean = false
        var dataDrivenItems: Boolean = false
        var experimentalMolangFeatures: Boolean = false
        var gametest: Boolean = false
        var upcomingCreatorFeatures: Boolean = false
        var villagerTradesRebalance: Boolean = false
    }

    class WorldPolicies
}
