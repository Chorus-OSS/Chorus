package org.chorus_oss.chorus.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.annotations.TomlComments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
class ChorusTOML {
    @SerialName("settings")
    val baseSettings = BaseSettings()

    @SerialName("server-settings")
    val serverSettings = ServerSettings()

    @SerialName("network-settings")
    val networkSettings = NetworkSettings()

    @SerialName("debug-settings")
    val debugSettings = DebugSettings()

    @SerialName("level-settings")
    val levelSettings = LevelSettings()

    @SerialName("chunk-settings")
    val chunkSettings = ChunkSettings()

    @SerialName("player-settings")
    val playerSettings = PlayerSettings()

    @SerialName("gameplay-settings")
    val gameplaySettings = GameplaySettings()

    @Serializable
    class BaseSettings {
        var language: String = "eng"

        @TomlComments("Use server-side translation for text")
        @SerialName("force-server-translate")
        var forceServerTranslate: Boolean = false

        @TomlComments("Message sent to clients on shutdown")
        @SerialName("shutdown-message")
        var shutdownMessage: String = "Server closed"

        @TomlComments("Generate warnings for deprecated events in plugins")
        @SerialName("deprecated-verbose")
        var deprecatedVerbose: Boolean = true

        @TomlComments("Number of threads used by the server scheduler. \"auto\" means automatic allocation")
        var asyncWorkers: String = "auto"

        @TomlComments("Safe player spawn")
        @SerialName("safe-spawn")
        var safeSpawn: Boolean = true

        @SerialName("waterdog-pe")
        var waterdogpe: Boolean = false

        @TomlComments("Autosave interval in ticks (1 = 20ms)")
        @SerialName("autosave")
        var autosave: Int = 6000

        @TomlComments("If unknown blocks should be saved to the leveldb")
        @SerialName("save-unknown-block")
        var saveUnknownBlock: Boolean = true
    }

    @Serializable
    data class ServerSettings(
        var motd: String = "Chorus Server",
        @SerialName("sub-motd")
        var subMotd: String = "chorus-oss.org",
        var ip: String = "0.0.0.0",
        var port: Int = 19132,
        @SerialName("max-players")
        var maxPlayers: Int = 10,
        @SerialName("xbox-auth")
        var xboxAuth: Boolean = true,
        @SerialName("check-login-time")
        var checkLoginTime: Boolean = false,
        @SerialName("white-list")
        var whiteList: Boolean = false,
        @SerialName("force-resources")
        var forceResources: Boolean = false,
        @SerialName("force-resources-allow-client-packs")
        var forceResourcesAllowClientPacks: Boolean = false,
        @SerialName("server-authoritative-movement")
        var serverAuthoritativeMovement: String = "server-auth"
    )

    @Serializable
    class NetworkSettings {
        var encryption: Boolean = true

        @SerialName("compression-level")
        var compressionLevel: Int = 7

        @TomlComments("Use Snappy compression (Not recommended)")
        var snappy: Boolean = false

        @TomlComments("Maximum compression buffer size")
        @SerialName("compression-buffer-size")
        var compressionBufferSize: Int = 1048576

        @TomlComments("Maximum decompression buffer size")
        @SerialName("max-decompress-size")
        var maxDecompressSize: Int = 67108864

        @TomlComments("Maximum amount of packets per second")
        @SerialName("packet-limit")
        var packetLimit: Int = 240
    }

    @Serializable
    class DebugSettings {
        @TomlComments(
            "Server's log level:",
            "- INFO",
            "- DEBUG",
            "- TRACE",
            "- ALL"
        )
        var level: String = "INFO"

        @TomlComments("Enable debug commands")
        var command: Boolean = false

        @TomlComments("Packets to ignore when logging at TRACE level")
        @SerialName("ignored-packets")
        var ignoredPackets: ArrayList<String> = ArrayList()

        @TomlComments("Packets to always log when logging at any level")
        @SerialName("logged-packets")
        var loggedPackets: List<String> = emptyList()

        @SerialName("allow-beta")
        var allowBeta: Boolean = false
    }

    @Serializable
    class LevelSettings {
        @TomlComments("Settings to use for the default level")
        @SerialName("default")
        var default: DefaultSettings = DefaultSettings()

        @TomlComments("Enable dynamic tick rate")
        @SerialName("auto-tick-rate")
        var autoTickRate: Boolean = true

        @TomlComments("Maximum dynamic tick rate")
        @SerialName("auto-tick-rate-limit")
        var autoTickRateLimit: Int = 20

        @TomlComments("Minimum dynamic tick rate")
        @SerialName("base-tick-rate")
        var baseTickRate: Int = 1

        @TomlComments("Should players be ticked")
        @SerialName("always-tick-players")
        var alwaysTickPlayers: Boolean = false

        @TomlComments("Enable redstone functionality")
        @SerialName("enable-redstone")
        var enableRedstone: Boolean = true

        @TomlComments("Should redstone be ticked")
        @SerialName("tick-redstone")
        var tickRedstone: Boolean = true

        @TomlComments("Chunk unloading delay (in ms)")
        @SerialName("chunk-unload-delay")
        var chunkUnloadDelay: Int = 15000

        @TomlComments("Enable level multi-threading")
        @SerialName("level-thread")
        var levelThread: Boolean = false

        @Serializable
        class DefaultSettings {
            var name: String = "world"

            var seed: String = ""

            @SerialName("spawn-protection")
            var spawnProtection: Int = 16

            @SerialName("allow-nether")
            var allowNether: Boolean = true

            @SerialName("allow-the-end")
            var allowTheEnd: Boolean = true

            @SerialName("auto-save")
            var autoSave: Boolean = true

            @SerialName("spawn-mobs")
            var spawnMobs: Boolean = true

            @SerialName("spawn-monsters")
            var spawnMonsters: Boolean = true

            @SerialName("view-distance")
            var viewDistance: Int = 8

            var achievements: Boolean = true

            @SerialName("announce-achievements")
            var announceAchievements: Boolean = true

            @SerialName("allow-flight")
            var allowFlight: Boolean = false

            var gamemode: Int = 0

            @SerialName("force-gamemode")
            var forceGamemode: Boolean = false

            var hardcore: Boolean = false

            var pvp: Boolean = true

            var difficulty: Int = 1
        }
    }

    @Serializable
    class ChunkSettings {
        @TomlComments("Maximum number of chunks sent to players per tick")
        @SerialName("per-tick-send")
        var perTickSend: Int = 8

        @TomlComments("Number of chunks a player needs to receive upon first spawn")
        @SerialName("spawn-threshold")
        var spawnThreshold: Int = 56

        @TomlComments("Tick cycle of chunks")
        @SerialName("chunks-per-ticks")
        var chunksPerTicks: Int = 40

        @TomlComments("Tick radius of chunks")
        @SerialName("tick-radius")
        var tickRadius: Int = 3

        @TomlComments("Whether chunks perform light updates")
        @SerialName("light-updates")
        var lightUpdates: Boolean = true

        @TomlComments("Whether to clear the tick list at the end of each tick")
        @SerialName("clear-tick-list")
        var clearTickList: Boolean = false

        @TomlComments("Maximum number of terrain generation tasks executed simultaneously")
        @SerialName("generation-queue-size")
        var generationQueueSize: Int = 128
    }

    @Serializable
    class PlayerSettings {
        @TomlComments("Whether to save player data")
        @SerialName("save-player-data")
        var savePlayerData: Boolean = true

        @TomlComments("Cooldown time for players changing skins")
        @SerialName("skin-change-cooldown")
        var skinChangeCooldown: Int = 30

        @TomlComments("Whether to force trust player skins, allowing players to use third-party skins freely")
        @SerialName("force-skin-trusted")
        var forceSkinTrusted: Boolean = false

        @TomlComments("Whether to check player movement")
        @SerialName("check-movement")
        var checkMovement: Boolean = true

        @SerialName("spawn-radius")
        var spawnRadius: Int = 16
    }

    @Serializable
    class GameplaySettings {
        @TomlComments(
            "Whether to allow players to use command blocks",
            "Note: If enabled, per-world gamerules still apply."
        )
        @SerialName("enable-command-blocks")
        var enableCommandBlocks: Boolean = true
    }

    fun save(file: File) {
        file.writeText(Toml.encodeToString(this))
    }

    companion object {
        fun load(file: File): ChorusTOML {
            return Toml(inputConfig = TomlInputConfig(ignoreUnknownNames = true)).decodeFromString<ChorusTOML>(
                if (file.exists()) file.readText() else ""
            )
        }
    }
}
