package org.chorus_oss.chorus

import com.akuleshov7.ktoml.Toml
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import org.apache.commons.io.FileUtils
import org.chorus_oss.chorus.block.BlockComposter
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.ConsoleCommandSender
import org.chorus_oss.chorus.command.PluginIdentifiableCommand
import org.chorus_oss.chorus.command.SimpleCommandMap
import org.chorus_oss.chorus.command.defaults.WorldCommand
import org.chorus_oss.chorus.command.function.FunctionManager
import org.chorus_oss.chorus.compression.ZlibChooser.setProvider
import org.chorus_oss.chorus.config.ChorusTOML
import org.chorus_oss.chorus.console.ChorusConsole
import org.chorus_oss.chorus.dispenser.DispenseBehaviorRegister
import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.entity.data.profession.Profession
import org.chorus_oss.chorus.entity.data.property.EntityProperty
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.buildPacketData
import org.chorus_oss.chorus.entity.data.property.EntityProperty.Companion.buildPlayerProperty
import org.chorus_oss.chorus.event.HandlerList.Companion.unregisterAll
import org.chorus_oss.chorus.event.level.LevelInitEvent
import org.chorus_oss.chorus.event.level.LevelLoadEvent
import org.chorus_oss.chorus.event.player.PlayerLoginEvent
import org.chorus_oss.chorus.event.server.ServerStartedEvent
import org.chorus_oss.chorus.event.server.ServerStopEvent
import org.chorus_oss.chorus.experimental.network.Network
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.generated.resources.Res
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.lang.Lang
import org.chorus_oss.chorus.lang.LangCode
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.level.DimensionEnum
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.LevelConfig
import org.chorus_oss.chorus.level.format.LevelConfig.GeneratorConfig
import org.chorus_oss.chorus.level.format.LevelProviderManager.addProvider
import org.chorus_oss.chorus.level.format.LevelProviderManager.getProvider
import org.chorus_oss.chorus.level.format.LevelProviderManager.getProviderName
import org.chorus_oss.chorus.level.format.leveldb.LevelDBProvider
import org.chorus_oss.chorus.level.tickingarea.manager.SimpleTickingAreaManager
import org.chorus_oss.chorus.level.tickingarea.manager.TickingAreaManager
import org.chorus_oss.chorus.level.tickingarea.storage.JSONTickingAreaStorage
import org.chorus_oss.chorus.level.updater.block.BlockStateUpdaterBase
import org.chorus_oss.chorus.math.round
import org.chorus_oss.chorus.nbt.NBTIO.readCompressed
import org.chorus_oss.chorus.nbt.NBTIO.writeGZIPCompressed
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.protocol.types.PlayerInfo
import org.chorus_oss.chorus.network.protocol.types.XboxLivePlayerInfo
import org.chorus_oss.chorus.permission.BanList
import org.chorus_oss.chorus.permission.DefaultPermissions.registerCorePermissions
import org.chorus_oss.chorus.plugin.*
import org.chorus_oss.chorus.positiontracking.PositionTrackingService
import org.chorus_oss.chorus.recipe.Recipe
import org.chorus_oss.chorus.registry.RecipeRegistry
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.resourcepacks.ResourcePackManager
import org.chorus_oss.chorus.resourcepacks.loader.JarPluginResourcePackLoader
import org.chorus_oss.chorus.resourcepacks.loader.ZippedResourcePackLoader
import org.chorus_oss.chorus.scheduler.ServerScheduler
import org.chorus_oss.chorus.scheduler.Task
import org.chorus_oss.chorus.scoreboard.manager.IScoreboardManager
import org.chorus_oss.chorus.scoreboard.manager.ScoreboardManager
import org.chorus_oss.chorus.scoreboard.storage.JSONScoreboardStorage
import org.chorus_oss.chorus.tags.BiomeTags
import org.chorus_oss.chorus.tags.BlockTags
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.utils.*
import org.chorus_oss.chorus.utils.Utils.allThreadDumps
import org.chorus_oss.chorus.utils.Utils.getExceptionMessage
import org.chorus_oss.chorus.utils.Utils.readFile
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.CraftingDataPacket
import org.chorus_oss.protocol.types.Color
import org.iq80.leveldb.CompressionType
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a server object, global singleton.
 *
 * is instantiated in [Chorus] and later the instance object is obtained via [Server.instance].
 * The constructor method of [Server] performs a number of operations, including but not limited to initializing configuration files, creating threads, thread pools, start plugins, registering recipes, blocks, entities, items, etc.
 */
class Server internal constructor(
    val filePath: String,
    dataPath: String,
    pluginPath: String,
    predefinedLanguage: String?
) {
    val dataPath: String = File(dataPath).absolutePath + "/"
    val pluginPath: String = File(pluginPath).absolutePath + "/"
    val commandDataPath: String = File(dataPath).absolutePath + "/command_data"

    var bannedPlayers: BanList
        private set

    var bannedIPs: BanList
        private set

    var operators: Config
        private set

    var whitelist: Config
        private set

    private val isRunning = AtomicBoolean(true)
    private val busyingTime: MutableList<Long> = Collections.synchronizedList(mutableListOf())
    private var hasStopped = false

    val commandMap: SimpleCommandMap by lazy { SimpleCommandMap(this) }

    val pluginManager: PluginManager by lazy { PluginManager(this, this.commandMap) }

    var scheduler: ServerScheduler
        private set

    /**
     * A tick counter that records the number of ticks that have passed on the server
     */
    var tick: Int = 0
        private set

    var nextTick: Long = 0
        private set
    private val tickAverage = RollingFloatAverage(20)

    private val useAverage = RollingFloatAverage(20)
    private var maxTick = 20f
    private var maxUse = 0f

    private var console: ChorusConsole

    private var consoleJob: Job

    /**
     * FJP thread pool responsible for terrain generation, data compression and other computing tasks
     */
    var computeScope: CoroutineScope
        private set

    var resourcePackManager: ResourcePackManager
        private set

    var consoleSender: ConsoleCommandSender
        private set

    val scoreboardManager: IScoreboardManager by lazy { ScoreboardManager(JSONScoreboardStorage("$commandDataPath/scoreboard.json")) }

    val functionManager: FunctionManager by lazy { FunctionManager("$commandDataPath/functions") }

    val tickingAreaManager: TickingAreaManager by lazy { SimpleTickingAreaManager(JSONTickingAreaStorage(this.dataPath + "worlds/")) }

    var maxPlayers: Int = 0
        private set
    private var autoSave = true

    /**
     * 配置项是否检查登录时间.<P>Does the configuration item check the login time.
    </P> */
    var checkLoginTime: Boolean = false

    var network: Network
        private set

    private var serverAuthoritativeMovementMode = 0
    val allowFlight: Boolean
        get() = settings.levelSettings.default.allowFlight
    private var difficulty = Int.MAX_VALUE
    var defaultGamemode: Int = Int.MAX_VALUE
        /**
         * @return 获取默认gamemode<br></br>Get default gamemode
         */
        get() {
            if (field == Int.MAX_VALUE) {
                field = this.gamemode
            }
            return field
        }
        /**
         * Set default gamemode for the server.
         *
         * @param defaultGamemode the default gamemode
         */
        set(defaultGamemode) {
            field = defaultGamemode
            network.updateMessage()
        }
    private var autoSaveTicker = 0
    private var autoSaveTicks = 6000

    var lang: Lang
        private set

    var baseLangCode: LangCode
        private set

    var serverID: UUID
        private set

    private val players: MutableMap<io.ktor.network.sockets.InetSocketAddress?, Player> = ConcurrentHashMap()
    private val playerList: MutableMap<UUID, Player> = ConcurrentHashMap()

    private var positionTrackingService: PositionTrackingService? = null

    /**
     * @return 获得所有游戏世界<br></br>Get all the game world
     */
    val levels: MutableMap<Int, Level> = object : HashMap<Int, Level>() {
        override fun put(key: Int, value: Level): Level? {
            val result = super.put(key, value)
            levelArray = values.toTypedArray()
            return result
        }

        override fun remove(key: Int, value: Level): Boolean {
            val result = super.remove(key, value)
            levelArray = values.toTypedArray()
            return result
        }

        override fun remove(key: Int): Level? {
            val result = super.remove(key)
            levelArray = values.toTypedArray()
            return result
        }
    }
    private var levelArray: Array<Level> = emptyArray()

    val thread: Thread = Thread.currentThread()
    val launchTime: Long = System.currentTimeMillis()

    var settings: ChorusTOML
        private set
    private var watchdog: Watchdog? = null
    private var playerDataDB: DB

    /**default levels */
    var defaultLevel: Level? = null
        set(value) {
            if (value == null || (this.isLevelLoaded(value.getLevelName()) && value != field)) {
                field = value
            }
        }
    var defaultNether: Level? = null
    var defaultEnd: Level? = null

    val allowNether: Boolean
        get() = settings.levelSettings.default.allowNether
    val allowEnd: Boolean
        get() = settings.levelSettings.default.allowTheEnd

    /** */
    init {
        var predefinedLanguage1 = predefinedLanguage
        instance = this

        if (!File(dataPath + "worlds/").exists()) {
            File(dataPath + "worlds/").mkdirs()
        }
        if (!File(dataPath + "players/").exists()) {
            File(dataPath + "players/").mkdirs()
        }
        if (!File(pluginPath).exists()) {
            File(pluginPath).mkdirs()
        }
        if (!File(commandDataPath).exists()) {
            File(commandDataPath).mkdirs()
        }

        this.console = ChorusConsole(this)
        this.consoleJob = CoroutineScope(Dispatchers.Default).launch { console.start() }

        val config = File(this.dataPath + "chorus.toml")
        var chooseLanguage: String? = null
        if (!config.exists()) {
            log.info("{}Welcome! Please choose a language first!", TextFormat.GREEN)
            try {
                val languageList = runBlocking { Res.readBytes("files/language/language.list").inputStream() }
                val lines = readFile(languageList).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (line in lines) {
                    log.info(line)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            while (chooseLanguage == null) {
                val lang: String
                if (predefinedLanguage1 != null) {
                    log.info("Trying to load language from predefined language: {}", predefinedLanguage1)
                    lang = predefinedLanguage1
                } else {
                    lang = console.readLine()
                }

                try {
                    runBlocking {
                        try {
                            Res.readBytes("files/language/$lang/lang.json").inputStream()
                        } catch (_: Throwable) {
                            null
                        }
                    }.use { conf ->
                        if (conf != null) {
                            chooseLanguage = lang
                        } else if (predefinedLanguage1 != null) {
                            log.warn(
                                "No language found for predefined language: {}, please choose a valid language",
                                predefinedLanguage1
                            )
                            predefinedLanguage1 = null
                        }
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        } else {
            val configInstance = Config(config)
            chooseLanguage = configInstance.getString("settings.language", "eng")
        }
        this.lang = Lang(chooseLanguage)
        this.baseLangCode = mapInternalLang(chooseLanguage)
        log.info("Loading {}...", TextFormat.GREEN.toString() + "chorus.toml" + TextFormat.RESET)

        this.settings = ChorusTOML.load(config)
        this.settings.save(config)

        settings.baseSettings.language = chooseLanguage

        this.computeScope = CoroutineScope(Dispatchers.Default)

        levelArray = Level.EMPTY_ARRAY

        val targetLevel = org.apache.logging.log4j.Level.getLevel(settings.debugSettings.level)
        val currentLevel = Chorus.logLevel
        if (targetLevel != null && targetLevel.intLevel() > currentLevel!!.intLevel()) {
            Chorus.logLevel = targetLevel
        }

        this.checkLoginTime = settings.serverSettings.checkLoginTime

        log.info(this.lang.tr("language.selected", lang.name, lang.langName))
        log.info(
            this.lang.tr(
                "chorus.server.start",
                TextFormat.AQUA.toString() + this.version + TextFormat.RESET
            )
        )

        val poolSize: String = settings.baseSettings.asyncWorkers
        val poolSizeNumber = try {
            poolSize.toInt()
        } catch (e: Exception) {
            max(Runtime.getRuntime().availableProcessors().toDouble(), 4.0).toInt()
        }
        ServerScheduler.WORKERS = poolSizeNumber
        this.scheduler = ServerScheduler()

        setProvider(settings.networkSettings.zlibProvider)

        this.serverAuthoritativeMovementMode =
            when (settings.serverSettings.serverAuthoritativeMovement) {
                "client-auth" -> 0
                "server-auth" -> 1
                "server-auth-with-rewind" -> 2
                else -> throw IllegalArgumentException()
            }
        if (settings.baseSettings.waterdogpe) {
            this.checkLoginTime = false
        }
        this.operators = Config(this.dataPath + "ops.txt", Config.ENUM)
        this.whitelist = Config(this.dataPath + "white-list.txt", Config.ENUM)
        this.bannedPlayers = BanList(this.dataPath + "banned-players.json")
        bannedPlayers.load()
        this.bannedIPs = BanList(this.dataPath + "banned-ips.json")
        bannedIPs.load()
        this.maxPlayers = settings.serverSettings.maxPlayers
        this.setAutoSave(settings.levelSettings.default.autoSave)
        if (settings.levelSettings.default.hardcore && this.getDifficulty() < 3) {
            settings.levelSettings.default.difficulty = 3
        }

        log.info(
            this.lang.tr(
                "chorus.server.info",
                name,
                TextFormat.YELLOW.toString() + this.chorusVersion + TextFormat.RESET,
                apiVersion
            )
        )
        this.consoleSender = ConsoleCommandSender()

        run {
            Registries.POTION.init()
            Registries.ENTITY.init()
            Registries.BLOCKENTITY.init()
            Registries.BLOCKSTATE_ITEMMETA.init()
            Registries.ITEM_RUNTIMEID.init()
            Registries.BLOCK.init()
            Registries.BLOCKSTATE.init()
            Registries.ITEM.init()
            Registries.CREATIVE.init()
            Registries.BIOME.init()
            Registries.FUEL.init()
            Registries.GENERATOR.init()
            Registries.GENERATE_STAGE.init()
            Registries.EFFECT.init()
            Registries.RECIPE.init()
            Profession.init()
            BlockTags
            ItemTags
            BiomeTags
            BlockStateUpdaterBase
            Enchantment.init()
            Attribute.init()
            BlockComposter.init()
            DispenseBehaviorRegister.init()
        }

        // Convert legacy data before plugins get the chance to mess with it.
        try {
            playerDataDB = Iq80DBFactory.factory.open(
                File(dataPath, "players"), Options()
                    .createIfMissing(true)
                    .compressionType(CompressionType.ZLIB_RAW)
            )
        } catch (e: IOException) {
            log.error("", e)
            exitProcess(1)
        }
        this.resourcePackManager = ResourcePackManager(
            ZippedResourcePackLoader(File(Chorus.DATA_PATH, "resource_packs")),
            JarPluginResourcePackLoader(File(this.pluginPath))
        )
        pluginManager.subscribeToPermission(BROADCAST_CHANNEL_ADMINISTRATIVE, this.consoleSender)
        pluginManager.registerInterface(JavaPluginLoader::class.java)
        console.setExecutingCommands(true)

        try {
            log.debug("Loading position tracking service")
            this.positionTrackingService =
                PositionTrackingService(File(Chorus.DATA_PATH, "services/position_tracking_db"))
        } catch (e: IOException) {
            log.error("Failed to start the Position Tracking DB service!", e)
        }
        pluginManager.loadInternalPlugin()

        this.serverID = UUID.randomUUID()
        pluginManager.loadPlugins(this.pluginPath)

        this.enablePlugins(PluginLoadOrder.Startup)

        addProvider("leveldb", LevelDBProvider::class.java)

        loadLevels()

        this.network = Network(this)
        tickingAreaManager.loadAllTickingArea()

        if (this.defaultLevel == null) {
            log.error(this.lang.tr("chorus.level.defaultError"))
            this.forceShutdown()

            throw RuntimeException()
        }

        this.autoSaveTicks = settings.baseSettings.autosave

        this.enablePlugins(PluginLoadOrder.PostWorld)

        EntityProperty.init()
        buildPacketData()
        buildPlayerProperty()

        if (!System.getProperty("disableWatchdog", "false").toBoolean()) {
            this.watchdog = Watchdog(this, 60000) //60s
            watchdog!!.start()
        }
        Runtime.getRuntime().addShutdownHook(Thread { this.shutdown() })
        this.start()
    }

    val enabledNetworkEncryption: Boolean = settings.networkSettings.encryption

    private fun loadLevels() {
        val file = File(this.dataPath + "/worlds")
        if (!file.isDirectory) throw RuntimeException("worlds isn't directory")
        //load all world from `worlds` folder
        for (f in requireNotNull(file.listFiles { obj: File -> obj.isDirectory })) {
            val levelConfig = getLevelConfig(f.name)
            if (levelConfig != null && !levelConfig.enable) {
                continue
            }

            if (!this.loadLevel(f.name)) {
                this.generateLevel(f.name, null)
            }
        }

        if (this.defaultLevel == null) {
            var levelFolder = settings.levelSettings.default.name
            if (levelFolder.trim { it <= ' ' }.isEmpty()) {
                log.warn("level-name cannot be null, using default")
                levelFolder = "world"
                settings.levelSettings.default.name = levelFolder
            }

            if (!this.loadLevel(levelFolder)) {
                //default world not exist
                //generate the default world
                val generatorConfig = HashMap<Int, GeneratorConfig>()
                //spawn seed
                val seed: Long
                val seedString = settings.levelSettings.default.seed
                seed = try {
                    seedString.toLong()
                } catch (e: NumberFormatException) {
                    seedString.hashCode().toLong()
                }
                //todo nether the_end overworld
                generatorConfig[0] = GeneratorConfig(
                    "flat",
                    seed,
                    false,
                    LevelConfig.AntiXrayMode.LOW,
                    true,
                    DimensionEnum.OVERWORLD.dimensionData,
                    emptyMap()
                )
                val levelConfig = LevelConfig("leveldb", true, generatorConfig)
                this.generateLevel(levelFolder, levelConfig)
            }
            this.defaultLevel = this.getLevelByName(levelFolder)!!
        }
    }

    // region lifecycle & ticking - 生命周期与游戏刻
    /**
     * 重载服务器
     *
     *
     * Reload Server
     */
    fun reload() {
        log.info("Reloading...")
        log.info("Saving levels...")

        for (level in this.levelArray) {
            level.save()
        }

        scoreboardManager.save()
        pluginManager.disablePlugins()
        pluginManager.clearPlugins()
        commandMap.clearCommands()

        this.maxPlayers = settings.serverSettings.maxPlayers
        if (settings.levelSettings.default.hardcore && this.getDifficulty() < 3) {
            difficulty = 3
            settings.levelSettings.default.difficulty = difficulty
        }

        bannedIPs.load()
        bannedPlayers.load()
        this.reloadWhitelist()
        operators.reload()

        for (entry in bannedIPs.entries.values) {
            try {
                // TODO
//                network.blockAddress(InetAddress.getByName(entry.name), -1)
            } catch (e: UnknownHostException) {
                // ignore
            }
        }

        pluginManager.registerInterface(JavaPluginLoader::class.java)
        //todo enable js plugin when adapt
//        JSIInitiator.reset();
//        JSFeatures.clearFeatures();
//        JSFeatures.initInternalFeatures();
        scoreboardManager.read()

        log.info("Reloading Registries...")
        run {
            Registries.POTION.reload()
            Registries.ENTITY.reload()
            Registries.BLOCKENTITY.reload()
            Registries.BLOCKSTATE_ITEMMETA.reload()
            Registries.BLOCKSTATE.reload()
            Registries.ITEM_RUNTIMEID.reload()
            Registries.BLOCK.reload()
            Registries.ITEM.reload()
            Registries.CREATIVE.reload()
            Registries.BIOME.reload()
            Registries.FUEL.reload()
            Registries.GENERATOR.reload()
            Registries.GENERATE_STAGE.reload()
            Registries.EFFECT.reload()
            Registries.RECIPE.reload()
            Enchantment.reload()
        }

        pluginManager.loadPlugins(this.pluginPath)
        functionManager.reload()

        this.enablePlugins(PluginLoadOrder.Startup)
        this.enablePlugins(PluginLoadOrder.PostWorld)
        pluginManager.callEvent(ServerStartedEvent())
    }

    /**
     * 关闭服务器
     *
     *
     * Shut down the server
     */
    fun shutdown() {
        isRunning.compareAndSet(true, false)
    }

    /**
     * 强制关闭服务器
     *
     *
     * Force Shut down the server
     */
    fun forceShutdown() {
        if (this.hasStopped) {
            return
        }

        try {
            isRunning.compareAndSet(true, false)

            this.hasStopped = true

            pluginManager.callEvent(ServerStopEvent())

            for (player in ArrayList(players.values)) {
                player.close(player.leaveMessage, settings.baseSettings.shutdownMessage)
            }

            val config = File(this.dataPath + "chorus.toml")
            config.writeText(Toml.encodeToString<ChorusTOML>(this.settings))

            log.debug("Disabling all plugins")
            pluginManager.disablePlugins()

            log.debug("Removing event handlers")
            unregisterAll()

            log.debug("Saving scoreboards data")
            scoreboardManager.save()

            log.debug("Stopping all tasks")
            scheduler.cancelAllTasks()
            scheduler.mainThreadHeartbeat((this.nextTick + 10000).toInt())

            log.debug("Unloading all levels")
            for (level in this.levelArray) {
                this.unloadLevel(level, true)
                while (level.isThreadRunning) runBlocking { delay(1) }
            }
            val tempPositionTrackingService = positionTrackingService
            if (tempPositionTrackingService != null) {
                log.debug("Closing position tracking service")
                tempPositionTrackingService.close()
            }

            log.debug("Closing console")
            consoleJob.cancel()

            log.debug("Stopping network interfaces")
            network.shutdown()
            playerDataDB.close()
            // close watchdog and metrics
            if (this.watchdog != null) {
                watchdog!!.running = false
            }
            // close threadPool
            computeScope.cancel()
            // todo other things
        } catch (e: Exception) {
            log.error("Exception happened while shutting down, exiting the process", e)
            exitProcess(1)
        }
    }

    fun start() {
//        BlockDefinitionGenerator.run()

        for (entry in bannedIPs.entries.values) {
            try {
                // TODO
//                network.blockAddress(InetAddress.getByName(entry.name))
            } catch (ignore: UnknownHostException) {
            }
        }
        this.tick = 0

        log.info(
            this.lang.tr(
                "chorus.server.defaultGameMode", getGamemodeString(
                    gamemode
                )
            )
        )
        log.info(
            this.lang.tr(
                "chorus.server.networkStart",
                TextFormat.YELLOW.toString() + (ip.ifEmpty { "*" }),
                TextFormat.YELLOW.toString() + port.toString()
            )
        )
        log.info(
            this.lang.tr(
                "chorus.server.startFinished",
                ((System.currentTimeMillis() - Chorus.START_TIME).toDouble() / 1000).toString()
            )
        )

        pluginManager.callEvent(ServerStartedEvent())
        this.tickProcessor()
        this.forceShutdown()
    }

    fun tickProcessor() {
        scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, object : Task() {
            override fun onRun(currentTick: Int) {
                System.gc()
            }
        }, 60)

        this.nextTick = System.currentTimeMillis()
        try {
            while (isRunning.get()) {
                try {
                    this.tick()

                    val next = this.nextTick
                    val current = System.currentTimeMillis()

                    if (next - 0.1 > current) {
                        val allocated = next - current - 1
                        if (allocated > 0) {
                            Thread.sleep(allocated, 900000)
                        }
                    }
                } catch (e: RuntimeException) {
                    log.error("A RuntimeException happened while ticking the server", e)
                }
            }
        } catch (e: Throwable) {
            log.error("Exception happened while ticking server\n{}", allThreadDumps, e)
        }
    }

    private fun checkTickUpdates(currentTick: Int) {
        if (settings.levelSettings.alwaysTickPlayers) {
            for (p in players.values) {
                p.onUpdate(currentTick)
            }
        }

        val baseTickRate: Int = settings.levelSettings.baseTickRate
        //Do level ticks if level threading is disabled
        if (!settings.levelSettings.levelThread) {
            for (level in levels.values) {
                if (level.tickRate > baseTickRate && --level.tickRateCounter > 0) {
                    continue
                }

                try {
                    val levelTime = System.currentTimeMillis()
                    //Ensures that the server won't try to tick a level without providers.
                    level.doTick(currentTick)
                    val tickMs = (System.currentTimeMillis() - levelTime).toInt()
                    level.tickRateTime = tickMs
                    if ((currentTick and 511) == 0) { // % 511
                        level.tickRateOptDelay = level.recalcTickOptDelay()
                    }

                    if (settings.levelSettings.autoTickRate) {
                        if (tickMs < 50 && level.tickRate > baseTickRate) {
                            val r = level.tickRate - 1
                            level.tickRate = r
                            if (r > baseTickRate) {
                                level.tickRateCounter = level.tickRate
                            }
                            log.debug(
                                "Raising level \"{}\" tick rate to {} ticks",
                                level.getLevelName(),
                                level.tickRate
                            )
                        } else if (tickMs >= 50) {
                            val autoTickRateLimit: Int = settings.levelSettings.autoTickRateLimit
                            if (level.tickRate == baseTickRate) {
                                level.tickRate = max(
                                    (baseTickRate + 1).toDouble(),
                                    min(autoTickRateLimit.toDouble(), (tickMs / 50).toDouble())
                                ).toInt()
                            } else if ((tickMs / level.tickRate) >= 50 && level.tickRate < autoTickRateLimit) {
                                level.tickRate += 1
                            }
                            log.debug(
                                "Level \"{}\" took {}ms, setting tick rate to {} ticks",
                                level.getLevelName(),
                                round(tickMs.toDouble(), 2),
                                level.tickRate
                            )
                            level.tickRateCounter = level.tickRate
                        }
                    }
                } catch (e: Exception) {
                    log.error(
                        this.lang.tr(
                            "chorus.level.tickError",
                            level.folderPath, getExceptionMessage(e)
                        ), e
                    )
                }
            }
        }
    }

    fun doAutoSave() {
        if (this.getAutoSave()) {
            for (player in ArrayList(players.values)) {
                if (player.isOnline) {
                    player.save(true)
                } else if (!player.isConnected()) {
                    this.removePlayer(player)
                }
            }

            for (level in this.levelArray) {
                level.save()
            }
            scoreboardManager.save()
        }
    }

    private fun tick() {
        val tickTime = System.currentTimeMillis()

        val time = tickTime - this.nextTick
        if (time < -25) {
            try {
                Thread.sleep(max(5.0, (-time - 25).toDouble()).toLong())
            } catch (e: InterruptedException) {
                log.debug("The thread {} got interrupted", Thread.currentThread().name, e)
            }
        }

        val tickTimeNano = System.nanoTime()
        if ((tickTime - this.nextTick) < -25) {
            return
        }

        ++this.tick
        network.tick()

        scheduler.mainThreadHeartbeat(this.tick)

        this.checkTickUpdates(this.tick)

        if ((this.tick and 15) == 0) {
            this.titleTick()
            this.maxTick = 20f
            this.maxUse = 0f
        }

        if (this.autoSave && ++this.autoSaveTicker >= this.autoSaveTicks) {
            this.autoSaveTicker = 0
            this.doAutoSave()
        }

        val nowNano = System.nanoTime()
        val tick = min(20.0, 1000000000 / max(1000000.0, (nowNano.toDouble() - tickTimeNano))).toFloat()
        val use = min(1.0, ((nowNano - tickTimeNano).toDouble()) / 50000000).toFloat()

        if (this.maxTick > tick) {
            this.maxTick = tick
        }

        if (this.maxUse < use) {
            this.maxUse = use
        }

        tickAverage.add(tick)
        useAverage.add(use)

        if ((this.nextTick - tickTime) < -1000) {
            this.nextTick = tickTime
        } else {
            this.nextTick += 50
        }
    }

    val ticksPerSecond: Float
        get() = ((this.maxTick * 100).roundToInt().toFloat()) / 100

    val ticksPerSecondAverage: Float
        get() = tickAverage.getAverage()

    val tickUsage: Float
        get() = round((this.maxUse * 100).toDouble()).toFloat()

    val tickUsageAverage: Float
        get() = useAverage.getAverage()

    // TODO: Fix title tick
    fun titleTick() {
        if (!Chorus.ANSI || !Chorus.TITLE) {
            return
        }

        val runtime = Runtime.getRuntime()
        val used = round((runtime.totalMemory() - runtime.freeMemory()).toDouble() / 1024 / 1024, 2)
        val max = round((runtime.maxMemory().toDouble()) / 1024 / 1024, 2)
        val usage = (used / max * 100).roundToInt().toString() + "%"
        var title = (0x1b.toChar().toString() + "]0;" + this.name + " "
                + this.chorusVersion
                + " | Online " + players.size + "/" + this.maxPlayers
                + " | Memory " + usage)
//        if (!Chorus.shortTitle) {
//            title += (" | U " + round((network.upload / 1024 * 1000), 2)
//                    + " D " + round((network.download / 1024 * 1000), 2) + " kB/s")
//        }
        title += (" | TPS " + this.ticksPerSecond
                + " | Load " + this.tickUsage + "%" + 0x07.toChar())

        print(title)
    }

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    /**
     * 将服务器设置为繁忙状态，这可以阻止相关代码认为服务器处于无响应状态。
     * 请牢记，必须在设置之后清除。
     *
     * @param busyTime 单位为毫秒
     * @return id
     */
    fun addBusying(busyTime: Long): Int {
        busyingTime.add(busyTime)
        return busyingTime.size - 1
    }

    fun removeBusying(index: Int) {
        busyingTime.removeAt(index)
    }

    fun getBusyingTime(): Long {
        if (busyingTime.isEmpty()) {
            return -1
        }
        return busyingTime[busyingTime.size - 1]
    }

    // endregion
    // region chat & commands - 聊天与命令
    /**
     * 广播一条消息给所有玩家
     *
     *Broadcast a message to all players
     *
     * @param message 消息
     * @return int 玩家数量<br></br>Number of players
     */
    fun broadcastMessage(message: String): Int {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS)
    }

    /**
     * @see .broadcastMessage
     */
    fun broadcastMessage(message: TextContainer): Int {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS)
    }

    /**
     * 广播一条消息给指定的[recipients][CommandSender]
     *
     *Broadcast a message to the specified [recipients][CommandSender]
     *
     * @param message 消息
     * @return int [recipients][CommandSender]数量<br></br>Number of [recipients][CommandSender]
     */
    fun broadcastMessage(message: String, recipients: Array<CommandSender>): Int {
        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        return recipients.size
    }

    /**
     * @see .broadcastMessage
     */
    fun broadcastMessage(message: String, recipients: Collection<CommandSender>): Int {
        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        return recipients.size
    }

    /**
     * @see .broadcastMessage
     */
    fun broadcastMessage(message: TextContainer, recipients: Collection<CommandSender>): Int {
        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        return recipients.size
    }

    /**
     * 从指定的许可名获取发送者们，广播一条消息给他们.可以指定多个许可名，以** ; **分割.<br></br>
     * 一个permission在`PluginManager#permSubs`对应一个[发送者][CommandSender]Set.
     *
     *
     * Get the sender to broadcast a message from the specified permission name, multiple permissions can be specified, split by ** ; **<br></br>
     * The permission corresponds to a [Sender][CommandSender] set in `PluginManager#permSubs`.
     *
     * @param message     消息内容<br></br>Message content
     * @param permissions 许可名，需要先通过[subscribeToPermission][PluginManager.subscribeToPermission]注册<br></br>Permissions name, need to register first through [subscribeToPermission][PluginManager.subscribeToPermission]
     * @return int 接受到消息的[发送者][CommandSender]数量<br></br>Number of [senders][CommandSender] who received the message
     */
    fun broadcast(message: String, permissions: String): Int {
        val recipients: MutableSet<CommandSender> = HashSet()

        for (permission in permissions.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            for (permissible in pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible is CommandSender && permissible.hasPermission(permission)) {
                    recipients.add(permissible)
                }
            }
        }

        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        return recipients.size
    }

    /**
     * @see .broadcast
     */
    fun broadcast(message: TextContainer, permissions: String): Int {
        val recipients: MutableSet<CommandSender> = HashSet()

        for (permission in permissions.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            for (permissible in pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible is CommandSender && permissible.hasPermission(permission)) {
                    recipients.add(permissible)
                }
            }
        }

        for (recipient in recipients) {
            recipient.sendMessage(message)
        }

        return recipients.size
    }

    /**
     * 以sender身份执行一行命令
     *
     *
     * Execute one line of command as sender
     *
     * @param sender      命令执行者
     * @param commandLine 一行命令
     * @return 返回0代表执行失败, 返回大于等于1代表执行成功<br></br>Returns 0 for failed execution, greater than or equal to 1 for successful execution
     * @throws ServerException 服务器异常
     */
    @Throws(ServerException::class)
    fun executeCommand(sender: CommandSender, commandLine: String): Int {
        // First we need to check if this command is on the main thread or not, if not, merge it in the main thread.
        if (!this.isPrimaryThread()) {
            scheduler.scheduleTask(null) { executeCommand(sender, commandLine) }
            return 1
        }

        var cmd = commandLine.trimStart()
        if (cmd.isEmpty()) {
            return 0
        }
        cmd = if (cmd[0] == '/') cmd.substring(1) else cmd

        return commandMap.executeCommand(sender, cmd)
    }

    /**
     * 以该控制台身份静音执行这些命令，无视权限
     *
     *
     * Execute these commands silently as the console, ignoring permissions.
     *
     * @param commands the commands
     * @throws ServerException 服务器异常
     */
    fun silentExecuteCommand(vararg commands: String?) {
        this.silentExecuteCommand(null, *commands)
    }

    /**
     * 以该玩家身份静音执行这些命令无视权限
     *
     *
     * Execute these commands silently as this player, ignoring permissions.
     *
     * @param sender   命令执行者<br></br>command sender
     * @param commands the commands
     * @throws ServerException 服务器异常
     */
    fun silentExecuteCommand(sender: Player?, vararg commands: String) {
        val revert = ArrayList<Level>()
        val server = instance
        for (level in server.levels.values) {
            if (level.gameRules.getBoolean(GameRule.SEND_COMMAND_FEEDBACK)) {
                level.gameRules.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false)
                revert.add(level)
            }
        }
        if (sender == null) {
            for (cmd in commands) {
                server.executeCommand(server.consoleSender, cmd)
            }
        } else {
            for (cmd in commands) {
                server.executeCommand(
                    server.consoleSender,
                    "execute as " + "\"" + sender.getEntityName() + "\" run " + cmd
                )
            }
        }

        for (level in revert) {
            level.gameRules.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true)
        }
    }

    fun getPluginCommand(name: String): PluginIdentifiableCommand? {
        val command = commandMap.getCommand(name)
        return command as? PluginIdentifiableCommand
    }

    // endregion
    // region plugins - 插件相关
    /**
     * 以指定插件加载顺序启用插件
     *
     *
     * Enable plugins in the specified plugin loading order
     *
     * @param type 插件加载顺序<br></br>Plugin loading order
     */
    fun enablePlugins(type: PluginLoadOrder) {
        for (plugin in ArrayList(pluginManager.plugins.values)) {
            if (!plugin.isEnabled && type == plugin.description.order) {
                this.enablePlugin(plugin)
            }
        }

        if (type == PluginLoadOrder.PostWorld) {
            registerCorePermissions()
        }
    }

    /**
     * 启用一个指定插件
     *
     *
     * Enable a specified plugin
     *
     * @param plugin 插件实例<br></br>Plugin instance
     */
    fun enablePlugin(plugin: Plugin) {
        pluginManager.enablePlugin(plugin)
    }

    /**
     * 禁用全部插件
     *
     *Disable all plugins
     */
    fun disablePlugins() {
        pluginManager.disablePlugins()
    }

    // endregion
    // region Players - 玩家相关
    fun onPlayerCompleteLoginSequence(player: Player) {
        this.sendFullPlayerListData(player)
    }

    fun onPlayerLogin(socketAddress: io.ktor.network.sockets.InetSocketAddress?, player: Player) {
        val ev: PlayerLoginEvent
        pluginManager.callEvent(PlayerLoginEvent(player, "Plugin reason").also { ev = it })
        if (ev.cancelled) {
            player.close(player.leaveMessage, ev.kickMessage)
            return
        }

        players[socketAddress] = player
    }

    @ApiStatus.Internal
    fun addOnlinePlayer(player: Player) {
        playerList[player.getUUID()] = player
        this.updatePlayerListData(
            player.getUUID(),
            player.getRuntimeID(),
            player.displayName,
            player.skin,
            player.loginChainData.xuid
        )
        network.updateMessage()
    }

    @OptIn(ExperimentalUuidApi::class)
    @ApiStatus.Internal
    fun removeOnlinePlayer(player: Player) {
        if (playerList.containsKey(player.getUUID())) {
            playerList.remove(player.getUUID())

            val pk = org.chorus_oss.protocol.packets.PlayerListPacket(
                actionType = org.chorus_oss.protocol.packets.PlayerListPacket.Companion.ActionType.Remove,
                addPlayerList = null,
                trustedSkinList = null,
                removePlayerList = listOf(
                    Uuid(player.getUUID())
                )
            )
            broadcastPacket(playerList.values, pk)
            network.updateMessage()
        }
    }

    /**
     * @see .updatePlayerListData
     */
    fun updatePlayerListData(uuid: UUID, entityId: Long, name: String, skin: Skin, players: Array<Player>) {
        this.updatePlayerListData(uuid, entityId, name, skin, "", players)
    }

    /**
     * Update PlayerListPacket data packets (i.e. player list data) for specified players
     *
     * @param uuid       uuid
     * @param entityId   实体id
     * @param name       名字
     * @param skin       皮肤
     * @param xboxUserId xbox用户id
     * @param players    指定接受数据包的玩家
     */
    @OptIn(ExperimentalUuidApi::class)
    fun updatePlayerListData(
        uuid: UUID,
        entityId: Long,
        name: String,
        skin: Skin,
        xboxUserId: String?,
        players: Array<Player>
    ) {
        // In some circumstances, the game sends confidential data in this string,
        // so under no circumstances should it be sent to all players on the server.
        skin.setSkinId("")

        val pk = org.chorus_oss.protocol.packets.PlayerListPacket(
            actionType = org.chorus_oss.protocol.packets.PlayerListPacket.Companion.ActionType.Add,
            addPlayerList = listOf(
                org.chorus_oss.protocol.packets.PlayerListPacket.Companion.AddPlayerEntry(
                    uuid = Uuid(uuid),
                    actorUniqueID = entityId,
                    playerName = name,
                    xuid = xboxUserId ?: "",
                    platformChatID = "",
                    buildPlatform = org.chorus_oss.protocol.types.Platform.Unknown,
                    skin = org.chorus_oss.protocol.types.skin.Skin(skin),
                    isTeacher = false,
                    isHost = false,
                    isSubClient = false,
                    playerColor = Color(
                        255.toByte(),
                        255.toByte(),
                        255.toByte(),
                        255.toByte()
                    )
                )
            ),
            trustedSkinList = listOf(skin.isTrusted() || settings.playerSettings.forceSkinTrusted),
            removePlayerList = null,
        )
        broadcastPacket(players.toList(), pk)
    }

    /**
     * @see .updatePlayerListData
     */
    /**
     * @see .updatePlayerListData
     */
    /**
     * @see .updatePlayerListData
     */
    @JvmOverloads
    fun updatePlayerListData(
        uuid: UUID,
        entityId: Long,
        name: String,
        skin: Skin,
        xboxUserId: String? = "",
        players: Collection<Player> = playerList.values
    ) {
        // In some circumstances, the game sends confidential data in this string,
        // so under no circumstances should it be sent to all players on the server.
        skin.setSkinId("")
        this.updatePlayerListData(
            uuid,
            entityId,
            name,
            skin,
            xboxUserId,
            players.toTypedArray()
        )
    }

    /**
     * 移除玩家数组中所有玩家的玩家列表数据.
     *
     *
     * Remove player list data for all players in the array.
     *
     * @param players 玩家数组
     */
    @OptIn(ExperimentalUuidApi::class)
    fun removePlayerListData(uuid: UUID, players: Array<Player>) {
        val pk = org.chorus_oss.protocol.packets.PlayerListPacket(
            actionType = org.chorus_oss.protocol.packets.PlayerListPacket.Companion.ActionType.Remove,
            addPlayerList = null,
            trustedSkinList = null,
            removePlayerList = listOf(
                Uuid(uuid)
            )
        )
        broadcastPacket(players.toList(), pk)
    }

    /**
     * 移除这个玩家的玩家列表数据.
     *
     *
     * Remove this player's player list data.
     *
     * @param player 玩家
     */
    @OptIn(ExperimentalUuidApi::class)
    fun removePlayerListData(uuid: UUID, player: Player) {
        val pk = org.chorus_oss.protocol.packets.PlayerListPacket(
            actionType = org.chorus_oss.protocol.packets.PlayerListPacket.Companion.ActionType.Remove,
            addPlayerList = null,
            trustedSkinList = null,
            removePlayerList = listOf(
                Uuid(uuid)
            )
        )
        player.sendPacket(pk)
    }

    @JvmOverloads
    fun removePlayerListData(uuid: UUID, players: Collection<Player> = playerList.values) {
        this.removePlayerListData(uuid, players.toTypedArray())
    }

    /**
     * 发送玩家列表数据包给一个玩家.
     *
     *
     * Send a player list packet to a player.
     *
     * @param player 玩家
     */
    @OptIn(ExperimentalUuidApi::class)
    fun sendFullPlayerListData(player: Player) {
        val players = playerList.values

        val pk = org.chorus_oss.protocol.packets.PlayerListPacket(
            actionType = org.chorus_oss.protocol.packets.PlayerListPacket.Companion.ActionType.Add,
            addPlayerList = players.map {
                org.chorus_oss.protocol.packets.PlayerListPacket.Companion.AddPlayerEntry(
                    uuid = Uuid(it.getUUID()),
                    actorUniqueID = it.getUniqueID(),
                    playerName = it.displayName,
                    xuid = it.loginChainData.xuid ?: "",
                    platformChatID = "",
                    buildPlatform = org.chorus_oss.protocol.types.Platform.Unknown,
                    skin = org.chorus_oss.protocol.types.skin.Skin(it.skin),
                    isTeacher = false,
                    isHost = false,
                    isSubClient = false,
                    playerColor = Color(
                        255.toByte(),
                        255.toByte(),
                        255.toByte(),
                        255.toByte()
                    )
                )
            },
            trustedSkinList = players.map {
                it.skin.isTrusted() || settings.playerSettings.forceSkinTrusted
            },
            removePlayerList = null,
        )

        player.sendPacket(pk)
    }

    /**
     * 从指定的UUID得到玩家实例.
     *
     *
     * Get the player instance from the specified UUID.
     *
     * @param uuid uuid
     * @return 玩家实例，可为空<br></br>Player example, can be empty
     */
    fun getPlayer(uuid: UUID): Optional<Player> {
        return Optional.ofNullable(playerList[uuid])
    }

    /**
     * 从数据库中查找指定玩家名对应的UUID.
     *
     *
     * Find the UUID corresponding to the specified player name from the database.
     *
     * @param name 玩家名<br></br>player name
     * @return 玩家的UUID，可为空.<br></br>The player's UUID, which can be empty.
     */
    fun lookupName(name: String): Optional<UUID> {
        val nameBytes = name.lowercase().toByteArray(StandardCharsets.UTF_8)
        val uuidBytes = playerDataDB[nameBytes] ?: return Optional.empty()

        if (uuidBytes.size != 16) {
            log.warn("Invalid uuid in name lookup database detected! Removing")
            playerDataDB.delete(nameBytes)
            return Optional.empty()
        }

        val buffer = ByteBuffer.wrap(uuidBytes)
        return Optional.of(UUID(buffer.getLong(), buffer.getLong()))
    }

    /**
     * 更新数据库中指定玩家名的UUID，若不存在则添加.
     *
     *
     * Update the UUID of the specified player name in the database, or add it if it does not exist.
     *
     * @param info the player info
     */
    fun updateName(info: PlayerInfo) {
        val uniqueId = info.uuid
        val name = info.username

        val nameBytes: ByteArray = name.lowercase(Locale.ENGLISH).toByteArray(StandardCharsets.UTF_8)

        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(uniqueId.mostSignificantBits)
        buffer.putLong(uniqueId.leastSignificantBits)
        val array = buffer.array()
        val bytes = playerDataDB[array]
        if (bytes == null) {
            playerDataDB.put(nameBytes, array)
        }
        val xboxAuthEnabled = settings.serverSettings.xboxAuth
        if (info is XboxLivePlayerInfo || !xboxAuthEnabled) {
            playerDataDB.put(nameBytes, array)
        }
    }

    fun getOfflinePlayer(name: String): IPlayer? {
        val result: IPlayer? = this.getPlayerExact(name.lowercase())
        if (result != null) {
            return result
        }

        return lookupName(name).map { uuid -> OfflinePlayer(uuid) }.orElse(null)
    }

    /**
     * 从指定的UUID得到一个玩家实例,可以是在线玩家也可以是离线玩家.
     *
     *
     * Get a player instance from the specified UUID, either online or offline.
     *
     * @param uuid uuid
     * @return 玩家<br></br>player
     */
    fun getOfflinePlayer(uuid: UUID): IPlayer {
        val onlinePlayer = getPlayer(uuid)
        if (onlinePlayer.isPresent) {
            return onlinePlayer.get()
        }

        return OfflinePlayer(uuid)
    }

    /**
     * create为false
     *
     *
     * create is false
     *
     * @see .getOfflinePlayerData
     */
    fun getOfflinePlayerData(uuid: UUID?): CompoundTag? {
        return getOfflinePlayerData(uuid, false)
    }

    /**
     * 获得UUID指定的玩家的NBT数据
     *
     * @param uuid   要获取数据的玩家UUID<br></br>UUID of the player to get data from
     * @param create 如果玩家数据不存在是否创建<br></br>If player data does not exist whether to create.
     * @return [CompoundTag]
     */
    fun getOfflinePlayerData(uuid: UUID?, create: Boolean): CompoundTag? {
        return getOfflinePlayerDataInternal(uuid, create)
    }

    fun getOfflinePlayerData(name: String): CompoundTag? {
        return getOfflinePlayerData(name, false)
    }

    fun getOfflinePlayerData(name: String, create: Boolean): CompoundTag? {
        val uuid = lookupName(name)
        if (uuid.isEmpty) {
            log.warn("Invalid uuid in name lookup database detected! Removing")
            playerDataDB.delete(name.toByteArray(StandardCharsets.UTF_8))
            return null
        }
        return getOfflinePlayerDataInternal(uuid.get(), create)
    }

    fun hasOfflinePlayerData(name: String): Boolean {
        val uuid = lookupName(name)
        if (uuid.isEmpty) {
            log.warn("Invalid uuid in name lookup database detected! Removing")
            playerDataDB.delete(name.toByteArray(StandardCharsets.UTF_8))
            return false
        }
        return hasOfflinePlayerData(uuid.get())
    }

    fun hasOfflinePlayerData(uuid: UUID): Boolean {
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        val bytes = playerDataDB[buffer.array()]
        return bytes != null
    }

    private fun getOfflinePlayerDataInternal(uuid: UUID?, create: Boolean): CompoundTag? {
        if (uuid == null) {
            log.error("UUID is empty, cannot query player data")
            return null
        }
        try {
            val buffer = ByteBuffer.allocate(16)
            buffer.putLong(uuid.mostSignificantBits)
            buffer.putLong(uuid.leastSignificantBits)
            val bytes = playerDataDB[buffer.array()]
            if (bytes != null) {
                return readCompressed(bytes)
            }
        } catch (e: IOException) {
            log.warn(this.lang.tr("chorus.data.playerCorrupted", uuid), e)
        }

        if (create) {
            if (settings.playerSettings.savePlayerData) {
                log.info(this.lang.tr("chorus.data.playerNotFound", uuid))
            }
            val spawn = defaultLevel!!.safeSpawn
            val nbt = CompoundTag()
                .putLong("firstPlayed", System.currentTimeMillis() / 1000)
                .putLong("lastPlayed", System.currentTimeMillis() / 1000)
                .putList(
                    "Pos", ListTag<FloatTag>()
                        .add(FloatTag(spawn.position.x))
                        .add(FloatTag(spawn.position.y))
                        .add(FloatTag(spawn.position.z))
                )
                .putString("Level", defaultLevel!!.getLevelName())
                .putList("Inventory", ListTag<Tag<*>>())
                .putCompound("Achievements", CompoundTag())
                .putInt("playerGameType", this.gamemode)
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putFloat("FallDistance", 0f)
                .putShort("Fire", 0)
                .putShort("Air", 300)
                .putBoolean("OnGround", true)
                .putBoolean("Invulnerable", false)

            this.saveOfflinePlayerData(uuid, nbt, true)
            return nbt
        } else {
            log.error("Player {} does not exist and cannot read playerdata", uuid)
            return null
        }
    }

    /**
     * @see .saveOfflinePlayerData
     */
    /**
     * @see .saveOfflinePlayerData
     */
    @JvmOverloads
    fun saveOfflinePlayerData(uuid: UUID, tag: CompoundTag, async: Boolean = false) {
        this.saveOfflinePlayerData(uuid.toString(), tag, async)
    }

    /**
     * @see .saveOfflinePlayerData
     */
    fun saveOfflinePlayerData(name: String, tag: CompoundTag) {
        this.saveOfflinePlayerData(name, tag, false)
    }

    /**
     * 保存玩家数据，玩家在线离线都行.
     *
     *
     * Save player data, players can be offline.
     *
     * @param nameOrUUid the name or uuid
     * @param tag        NBT数据<br></br>nbt data
     * @param async      是否异步保存<br></br>Whether to save asynchronously
     */
    fun saveOfflinePlayerData(nameOrUUid: String, tag: CompoundTag, async: Boolean) {
        val uuid = lookupName(nameOrUUid).orElse(UUID.fromString(nameOrUUid))
        if (settings.playerSettings.savePlayerData) {
            scheduler.scheduleTask(InternalPlugin.INSTANCE, object : Task() {
                val hasRun: AtomicBoolean = AtomicBoolean(false)

                override fun onRun(currentTick: Int) {
                    this.onCancel()
                }

                //doing it like this ensures that the playerdata will be saved in a server shutdown
                override fun onCancel() {
                    if (!hasRun.getAndSet(true)) {
                        saveOfflinePlayerDataInternal(tag, uuid)
                    }
                }
            }, async)
        }
    }

    private fun saveOfflinePlayerDataInternal(tag: CompoundTag, uuid: UUID) {
        try {
            val bytes = writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)
            val buffer = ByteBuffer.allocate(16)
            buffer.putLong(uuid.mostSignificantBits)
            buffer.putLong(uuid.leastSignificantBits)
            playerDataDB.put(buffer.array(), bytes)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 从玩家名获得一个在线玩家，这个方法是模糊匹配，只要玩家名带有name前缀就会被返回.
     *
     *
     * Get an online player from the player name, this method is a fuzzy match and will be returned as long as the player name has the name prefix.
     *
     * @param name 玩家名<br></br>player name
     * @return 玩家实例对象，获取失败为null<br></br>Player instance object,failed to get null
     */
    fun getPlayer(name: String): Player? {
        var name1 = name
        var found: Player? = null
        name1 = name1.lowercase()
        var delta = Int.MAX_VALUE
        for (player in onlinePlayers.values) {
            if (player.getEntityName().lowercase().startsWith(name1)) {
                val curDelta: Int = player.getEntityName().length - name1.length
                if (curDelta < delta) {
                    found = player
                    delta = curDelta
                }
                if (curDelta == 0) {
                    break
                }
            }
        }

        return found
    }

    /**
     * 从玩家名获得一个在线玩家，这个方法是精确匹配，当玩家名字符串完全相同时返回.
     *
     *
     * Get an online player from a player name, this method is an exact match and returns when the player name string is identical.
     *
     * @param name 玩家名<br></br>player name
     * @return 玩家实例对象，获取失败为null<br></br>Player instance object,failed to get null
     */
    fun getPlayerExact(name: String): Player? {
        var name1 = name
        name1 = name1.lowercase()
        for (player in onlinePlayers.values) {
            if (player.getEntityName().lowercase() == name1) {
                return player
            }
        }

        return null
    }

    /**
     * 指定一个部分玩家名，返回所有包含或者等于该名称的玩家.
     *
     *
     * Specify a partial player name and return all players with or equal to that name.
     *
     * @param partialName 部分玩家名<br></br>partial name
     * @return 匹配到的所有玩家, 若匹配不到则为一个空数组<br></br>All players matched, if not matched then an empty array
     */
    fun matchPlayer(partialName: String): Array<Player> {
        var partialName1 = partialName
        partialName1 = partialName1.lowercase()
        val matchedPlayer: MutableList<Player> = ArrayList()
        for (player in onlinePlayers.values) {
            if (player.getEntityName().lowercase() == partialName1) {
                return arrayOf(player)
            } else if (player.getEntityName().lowercase().contains(partialName1)) {
                matchedPlayer.add(player)
            }
        }

        return matchedPlayer.toTypedArray()
    }

    @ApiStatus.Internal
    fun removePlayer(player: Player) {
        val toRemove = players.remove(player.rawSocketAddress)
        if (toRemove != null) {
            return
        }

        for (socketAddress in ArrayList(players.keys)) {
            val p = players[socketAddress]
            if (player === p) {
                players.remove(socketAddress)
                break
            }
        }
    }

    val onlinePlayers: Map<UUID, Player>
        /**
         * 获得所有在线的玩家Map.
         *
         *
         * Get all online players Map.
         *
         * @return 所有的在线玩家Map
         */
        get() = ImmutableMap.copyOf(playerList)

    // endregion
    // region constants - 常量
    val name: String
        /**
         * @return 服务器名称<br></br>The name of server
         */
        get() = "Chorus"

    val chorusVersion
        get() = Chorus.VERSION

    val version: String
        get() = "v${ProtocolInfo.VERSION}"

    val apiVersion: String
        get() = Chorus.API_VERSION

    fun getPositionTrackingService(): PositionTrackingService {
        return positionTrackingService!!
    }

    // region crafting & recipe - 合成与配方
    /**
     * 发送配方列表数据包给一个玩家.
     *
     *
     * Send a recipe list packet to a player.
     *
     * @param player 玩家
     */
    fun sendRecipeList(player: Player) {
        player.session.sendRawPacket(CraftingDataPacket.id, Registries.RECIPE.packet)
    }

    /**
     * 注册配方到配方管理器
     *
     *
     * Register Recipe to Recipe Manager
     *
     * @param recipe 配方
     */
    fun addRecipe(recipe: Recipe) {
        Registries.RECIPE.register(recipe)
    }

    val recipeRegistry: RecipeRegistry
        get() = Registries.RECIPE

    // endregion
    // region Levels - 游戏世界相关

    var defaultNetherLevel: Level?
        /**
         * @return Get the default nether
         */
        get() = defaultNether
        /**
         * Set default nether
         */
        set(defaultLevel) {
            if (defaultLevel == null || (this.isLevelLoaded(defaultLevel.getLevelName()) && defaultLevel != this.defaultNether)) {
                this.defaultNether = defaultLevel
            }
        }

    var defaultEndLevel: Level?
        /**
         * @return Get the default the_end level
         */
        get() = defaultLevel
        /**
         * Set default the_end level
         */
        set(defaultLevel) {
            if (defaultLevel == null || (this.isLevelLoaded(defaultLevel.getLevelName()) && defaultLevel != this.defaultEnd)) {
                this.defaultEnd = defaultLevel
            }
        }

    /**
     * @param name 世界名字
     * @return 世界是否已经加载<br></br>Is the world already loaded
     */
    fun isLevelLoaded(name: String): Boolean {
        return this.getLevelByName(name) != null
    }

    /**
     * 从世界id得到世界,0主世界 1 地狱 2 末地
     *
     *
     * Get world from world id,0 OVERWORLD 1 NETHER 2 THE_END
     *
     * @param levelId 世界id<br></br>world id
     * @return level实例<br></br>level instance
     */
    fun getLevel(levelId: Int): Level? {
        if (levels.containsKey(levelId)) {
            return levels[levelId]
        }
        return null
    }

    /**
     * 从世界名得到世界,overworld 主世界 nether 地狱 the_end 末地
     *
     *
     * Get world from world name,`overworld nether the_end`
     *
     * @param name 世界名<br></br>world name
     * @return level实例<br></br>level instance
     */
    fun getLevelByName(name: String): Level? {
        for (level in this.levelArray) {
            if (level.getLevelName() == name) {
                return level
            }
        }
        return null
    }

    /**
     * 卸载世界
     *
     *
     * unload level
     *
     * @param level       世界
     * @param forceUnload 是否强制卸载<br></br>whether to force uninstallation.
     * @return 卸载是否成功
     */
    @JvmOverloads
    fun unloadLevel(level: Level, forceUnload: Boolean = false): Boolean {
        check(!(level == this.defaultLevel && !forceUnload)) { "The default level cannot be unloaded while running, please switch levels." }

        return level.unload(forceUnload)
    }

    fun getLevelConfig(levelFolderName: String): LevelConfig? {
        if (levelFolderName.trim { it <= ' ' } == "") {
            throw LevelException("Invalid empty level name")
        }
        var path = if (levelFolderName.contains("/") || levelFolderName.contains("\\")) {
            levelFolderName
        } else {
            File(this.dataPath, "worlds/$levelFolderName").absolutePath
        }
        val jpath = Path.of(path)
        path = jpath.toString()
        if (!jpath.toFile().exists()) {
            log.warn(this.lang.tr("chorus.level.notFound", levelFolderName))
            return null
        }

        val config = jpath.resolve("config.json").toFile()
        val levelConfig: LevelConfig
        if (config.exists()) {
            try {
                levelConfig = JSONUtils.from(config, LevelConfig::class.java)
                FileUtils.write(config, JSONUtils.toPretty(levelConfig), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else {
            //verify the provider
            val provider = getProvider(path)
            if (provider == null) {
                log.error(this.lang.tr("chorus.level.loadError", levelFolderName, "Unknown provider"))
                return null
            }
            val map: MutableMap<Int, GeneratorConfig> = HashMap()
            //todo nether the_end overworld
            map[0] = GeneratorConfig(
                "flat",
                System.currentTimeMillis(),
                false,
                LevelConfig.AntiXrayMode.LOW,
                true,
                DimensionEnum.OVERWORLD.dimensionData,
                emptyMap()
            )
            levelConfig = LevelConfig(getProviderName(provider), true, map)
            try {
                config.createNewFile()
                FileUtils.write(config, JSONUtils.toPretty(levelConfig), StandardCharsets.UTF_8)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        return levelConfig
    }

    /**
     * @param levelFolderName the level folder name
     * @return whether load success
     */
    fun loadLevel(levelFolderName: String): Boolean {
        val levelConfig = getLevelConfig(levelFolderName) ?: return false
        val path = if (levelFolderName.contains("/") || levelFolderName.contains("\\")) {
            levelFolderName
        } else {
            File(this.dataPath, "worlds/$levelFolderName").absolutePath
        }
        val pathS = Path.of(path).toString()

        val generators: Map<Int, GeneratorConfig> = levelConfig.generators
        for ((_, value) in generators) {
            val levelName = levelFolderName + if (generators.size > 1) value.dimensionData.suffix else ""
            if (this.isLevelLoaded(levelName)) {
                return true
            }
            val level: Level
            try {
                if (!LevelDBProvider.isValid(pathS)) {
                    log.error(this.lang.tr("chorus.level.loadError", levelFolderName, "the level does not exist"))
                    return false
                }
                level = Level(
                    levelName, pathS, generators.size, LevelDBProvider::class.java, value
                )
            } catch (e: Exception) {
                log.error(this.lang.tr("chorus.level.loadError", levelFolderName, e.message!!), e)
                return false
            }
            levels[level.id] = level
            level.initLevel()
            pluginManager.callEvent(LevelLoadEvent(level))
            level.tickRate = settings.levelSettings.baseTickRate
        }
        if (tick != 0) { //update world enum when load  
            WorldCommand.WORLD_NAME_ENUM.updateSoftEnum()
        }
        return true
    }

    fun generateLevel(name: String, levelConfig: LevelConfig?): Boolean {
        var levelConfig1 = levelConfig
        if (name.isBlank()) {
            return false
        }
        var path = if (name.contains("/") || name.contains("\\")) {
            name
        } else {
            dataPath + "worlds/" + name + "/"
        }

        val jpath = Path.of(path)
        path = jpath.toString()
        val config = jpath.resolve("config.json").toFile()
        if (config.exists()) {
            try {
                levelConfig1 = JSONUtils.from(FileReader(config), LevelConfig::class.java)
                FileUtils.write(config, JSONUtils.toPretty(levelConfig1), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                log.error("The levelConfig is not exists under the {} path", path)
                return false
            }
        } else if (levelConfig1 != null) {
            try {
                jpath.toFile().mkdirs()
                config.createNewFile()
                FileUtils.write(config, JSONUtils.toPretty<LevelConfig?>(levelConfig1), StandardCharsets.UTF_8)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        } else {
            log.error("The levelConfig is not specified and no config.json exists under the {} path", path)
            return false
        }

        for (entry in levelConfig1.generators.entries) {
            val generatorConfig: GeneratorConfig = entry.value
            val level: Level
            try {
                LevelDBProvider.generate(path, name, generatorConfig)
                val levelName = name + if (levelConfig1.generators.size > 1) entry.value.dimensionData.suffix else ""
                if (this.isLevelLoaded(levelName)) {
                    log.warn("level {} has already been loaded!", levelName)
                    continue
                }
                level =
                    Level(levelName, path, levelConfig1.generators.size, LevelDBProvider::class.java, generatorConfig)

                levels[level.id] = level
                level.initLevel()
                level.tickRate = settings.levelSettings.baseTickRate
                pluginManager.callEvent(LevelInitEvent(level))
                pluginManager.callEvent(LevelLoadEvent(level))
            } catch (e: Exception) {
                log.error(this.lang.tr("chorus.level.generationError", name, getExceptionMessage(e)), e)
                return false
            }
        }
        return true
    }

    fun addOp(name: String) {
        operators[name.lowercase()] = true
        val player = this.getPlayerExact(name)
        if (player != null) {
            player.recalculatePermissions()
            player.adventureSettings.onOpChange(true)
            player.adventureSettings.update()
            player.session.syncAvailableCommands()
        }
        operators.save(true)
    }

    fun removeOp(name: String) {
        operators.remove(name.lowercase())
        val player = this.getPlayerExact(name)
        if (player != null) {
            player.recalculatePermissions()
            player.adventureSettings.onOpChange(false)
            player.adventureSettings.update()
            player.session.syncAvailableCommands()
        }
        operators.save()
    }

    fun addWhitelist(name: String) {
        whitelist[name.lowercase()] = true
        whitelist.save(true)
    }

    fun removeWhitelist(name: String) {
        whitelist.remove(name.lowercase())
        whitelist.save(true)
    }

    fun isWhitelisted(name: String): Boolean {
        return !this.hasWhitelist() || operators.exists(name, true) || whitelist.exists(name, true)
    }

    fun isOp(name: String?): Boolean {
        return name != null && operators.exists(name, true)
    }

    fun reloadWhitelist() {
        whitelist.reload()
    }

    // endregion
    // region configs - 配置相关

    /**
     * Set the players count is allowed
     *
     * @param maxPlayers the max players
     */
    fun setMaxPlayers(maxPlayers: Int) {
        this.maxPlayers = maxPlayers
        network.updateMessage()
    }

    val port: Int
        /**
         * @return 服务器端口<br></br>server port
         */
        get() = settings.serverSettings.port

    val viewDistance: Int
        /**
         * @return 可视距离<br></br>server view distance
         */
        get() = settings.levelSettings.default.viewDistance

    val ip: String
        /**
         * @return 服务器网络地址<br></br>server ip
         */
        get() = settings.serverSettings.ip

    /**
     * @return 服务器是否会自动保存<br></br>Does the server automatically save
     */
    fun getAutoSave(): Boolean {
        return this.autoSave
    }

    /**
     * 设置服务器自动保存
     *
     *
     * Set server autosave
     *
     * @param autoSave 是否自动保存<br></br>Whether to save automatically
     */
    fun setAutoSave(autoSave: Boolean) {
        this.autoSave = autoSave
        for (level in this.levelArray) {
            level.autoSave = this.autoSave
        }
    }

    val gamemode: Int
        /**
         * 得到服务器的gamemode
         *
         *
         * Get the gamemode of the server
         *
         * @return gamemode id
         */
        get() = settings.levelSettings.default.gamemode and 3

    val forceGamemode: Boolean
        get() = settings.levelSettings.default.forceGamemode

    /**
     * 获得服务器游戏难度
     *
     *
     * Get server game difficulty
     *
     * @return 游戏难度id<br></br>game difficulty id
     */
    fun getDifficulty(): Int {
        if (this.difficulty == Int.MAX_VALUE) {
            this.difficulty = settings.levelSettings.default.difficulty
        }
        return this.difficulty
    }

    /**
     * 设置服务器游戏难度
     *
     *
     * set server game difficulty
     *
     * @param difficulty 游戏难度id<br></br>game difficulty id
     */
    fun setDifficulty(difficulty: Int) {
        var value = difficulty
        if (value < 0) value = 0
        if (value > 3) value = 3
        this.difficulty = value
        settings.levelSettings.default.difficulty = value
    }

    /**
     * @return 是否开启白名单<br></br>Whether to start server whitelist
     */
    fun hasWhitelist(): Boolean {
        return settings.serverSettings.whiteList
    }

    val spawnRadius: Int
        /**
         * @return 得到服务器出生点保护半径<br></br>Get server birth point protection radius
         */
        get() = settings.levelSettings.default.spawnProtection

    val isHardcore: Boolean
        /**
         * @return 服务器是否为硬核模式<br></br>Whether the server is in hardcore mode
         */
        get() = settings.levelSettings.default.hardcore

    var motd: String
        /**
         * @return 得到服务器标题<br></br>Get server motd
         */
        get() = settings.serverSettings.motd
        /**
         * Set the motd of server.
         *
         * @param motd the motd content
         */
        set(motd) {
            settings.serverSettings.motd = motd
            network.updateMessage()
        }

    var subMotd: String
        /**
         * @return 得到服务器子标题<br></br>Get the server subheading
         */
        get() {
            return settings.serverSettings.subMotd
        }
        /**
         * Set the sub motd of server.
         *
         * @param subMotd the sub motd
         */
        set(subMotd) {
            settings.serverSettings.subMotd = subMotd
            network.updateMessage()
        }

    val forceResources: Boolean
        /**
         * @return 是否强制使用服务器资源包<br></br>Whether to force the use of server resourcepack
         */
        get() = settings.serverSettings.forceResources

    val forceResourcesAllowOwnPacks: Boolean
        /**
         * @return 是否强制使用服务器资源包的同时允许加载客户端资源包<br></br>Whether to force the use of server resourcepack while allowing the loading of client resourcepack
         */
        get() = settings.serverSettings.forceResourcesAllowClientPacks

    private fun mapInternalLang(langName: String): LangCode {
        return when (langName) {
            "bra" -> LangCode.valueOf("pt_BR")
            "chs" -> LangCode.valueOf("zh_CN")
            "cht" -> LangCode.valueOf("zh_TW")
            "cze" -> LangCode.valueOf("cs_CZ")
            "deu" -> LangCode.valueOf("de_DE")
            "fin" -> LangCode.valueOf("fi_FI")
            "eng" -> LangCode.valueOf("en_US")
            "fra" -> LangCode.valueOf("fr_FR")
            "idn" -> LangCode.valueOf("id_ID")
            "jpn" -> LangCode.valueOf("ja_JP")
            "kor" -> LangCode.valueOf("ko_KR")
            "ltu" -> LangCode.valueOf("en_US")
            "pol" -> LangCode.valueOf("pl_PL")
            "rus" -> LangCode.valueOf("ru_RU")
            "spa" -> LangCode.valueOf("es_ES")
            "tur" -> LangCode.valueOf("tr_TR")
            "ukr" -> LangCode.valueOf("uk_UA")
            "vie" -> LangCode.valueOf("en_US")
            else -> throw IllegalArgumentException()
        }
    }

    fun isIgnoredPacket(packet: Packet): Boolean {
        return settings.debugSettings.ignoredPackets.contains(packet::class.java.simpleName)
    }

    fun isLoggedPacket(packet: Packet): Boolean {
        return settings.debugSettings.loggedPackets.contains(packet::class.java.simpleName)
    }

    fun getServerAuthoritativeMovement(): Int {
        return serverAuthoritativeMovementMode
    }

    // endregion
    // region threading - 并发基础设施
    /**
     * Checks the current thread against the expected primary thread for the
     * server.
     *
     *
     * **Note:** this method should not be used to indicate the current
     * synchronized state of the runtime. A current thread matching the main
     * thread indicates that it is synchronized, but a mismatch does not
     * preclude the same assumption.
     *
     * @return true if the current thread matches the expected primary thread,
     * false otherwise
     */
    fun isPrimaryThread(): Boolean {
        return (Thread.currentThread() === thread)
    }

    companion object : Loggable {
        const val BROADCAST_CHANNEL_ADMINISTRATIVE: String = "chorus.broadcast.admin"
        const val BROADCAST_CHANNEL_USERS: String = "chorus.broadcast.user"

        // endregion
        // region server singleton - Server 单例
        @JvmStatic
        lateinit var instance: Server
            private set

        // endregion
        // region networking - 网络相关
        /**
         * @see .broadcastPacket
         */
        fun broadcastPacket(players: Collection<Player>, packet: Packet) {
            for (player in players) {
                player.sendPacket(packet)
            }
        }

        /**
         * 广播一个数据包给指定的玩家们.
         *
         *Broadcast a packet to the specified players.
         *
         * @param players 接受数据包的所有玩家<br></br>All players receiving the data package
         * @param packet  数据包
         */
        fun broadcastPacket(players: Array<Player>, packet: Packet) {
            for (player in players) {
                player.sendPacket(packet)
            }
        }

        fun broadcastPacket(players: Iterable<Player>, packet: Packet) {
            for (player in players) {
                player.sendPacket(packet)
            }
        }

        /**`
         * 默认`direct=false`
         *
         * @see .getGamemodeString
         */
        fun getGamemodeString(mode: Int): String {
            return getGamemodeString(mode, false)
        }

        /**
         * 从gamemode id获取游戏模式字符串.
         *
         *
         * Get game mode string from gamemode id.
         *
         * @param mode   gamemode id
         * @param direct 如果为true就直接返回字符串,为false返回代表游戏模式的硬编码字符串.<br></br>If true, the string is returned directly, and if false, the hard-coded string representing the game mode is returned.
         * @return 游戏模式字符串<br></br>Game Mode String
         */
        fun getGamemodeString(mode: Int, direct: Boolean): String {
            return when (mode) {
                Player.SURVIVAL -> if (direct) "Survival" else "%gameMode.survival"
                Player.CREATIVE -> if (direct) "Creative" else "%gameMode.creative"
                Player.ADVENTURE -> if (direct) "Adventure" else "%gameMode.adventure"
                Player.SPECTATOR -> if (direct) "Spectator" else "%gameMode.spectator"
                else -> "UNKNOWN"
            }
        }

        /**
         * 从字符串获取gamemode
         *
         *
         * Get gamemode from string
         *
         * @param str 代表游戏模式的字符串，例如0,survival...<br></br>A string representing the game mode, e.g. 0,survival...
         * @return 游戏模式id<br></br>gamemode id
         */
        fun getGamemodeFromString(str: String): Int {
            return when (str.trim { it <= ' ' }.lowercase()) {
                "0", "survival", "s" -> Player.SURVIVAL
                "1", "creative", "c" -> Player.CREATIVE
                "2", "adventure", "a" -> Player.ADVENTURE
                "3", "spectator" -> Player.SPECTATOR
                else -> -1
            }
        }

        /**
         * 从字符串获取游戏难度
         *
         *
         * Get game difficulty from string
         *
         * @param str 代表游戏难度的字符串，例如0,peaceful...<br></br>A string representing the game difficulty, e.g. 0,peaceful...
         * @return 游戏难度id<br></br>game difficulty id
         */
        fun getDifficultyFromString(str: String): Int {
            when (str.trim { it <= ' ' }.lowercase()) {
                "0", "peaceful", "p" -> return 0

                "1", "easy", "e" -> return 1

                "2", "normal", "n" -> return 2

                "3", "hard", "h" -> return 3
            }
            return -1
        }
    }
}
