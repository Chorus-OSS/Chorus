package org.chorus_oss.chorus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.reflect.FieldUtils
import org.chorus_oss.chorus.block.BlockComposter
import org.chorus_oss.chorus.command.SimpleCommandMap
import org.chorus_oss.chorus.config.ChorusTOML
import org.chorus_oss.chorus.dispenser.DispenseBehaviorRegister
import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.entity.data.profession.Profession
import org.chorus_oss.chorus.inventory.HumanEnderChestInventory
import org.chorus_oss.chorus.inventory.HumanInventory
import org.chorus_oss.chorus.inventory.HumanOffHandInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.lang.Lang
import org.chorus_oss.chorus.level.DimensionEnum
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.LevelConfig
import org.chorus_oss.chorus.level.format.LevelConfig.GeneratorConfig
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.level.format.leveldb.LevelDBProvider
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.Network
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.DataPacketManager
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.types.PlayerInfo
import org.chorus_oss.chorus.permission.BanList
import org.chorus_oss.chorus.plugin.JavaPluginLoader
import org.chorus_oss.chorus.positiontracking.PositionTrackingService
import org.chorus_oss.chorus.registry.BlockRegistry
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.scheduler.ServerScheduler
import org.chorus_oss.chorus.utils.ClientChainData
import org.chorus_oss.protocol.core.Packet
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport


class GameMockExtension : MockitoExtension() {
    private lateinit var serverMockedStatic: MockedStatic<Server>

    override fun beforeEach(context: ExtensionContext) {
        serverMockedStatic = Mockito.mockStatic(Server::class.java)
        serverMockedStatic.`when`<Any>(Verification { Server.instance }).thenReturn(server)
        super.beforeEach(context)
    }

    override fun afterEach(context: ExtensionContext) {
        serverMockedStatic.close()
        super.afterEach(context)
    }

    @Throws(ParameterResolutionException::class)
    override fun supportsParameter(parameterContext: ParameterContext, context: ExtensionContext): Boolean {
        val b = super.supportsParameter(parameterContext, context)
        return b || parameterContext.parameter.type == GameMockExtension::class.java || parameterContext.parameter.type == BlockRegistry::class.java
                || parameterContext.parameter.type == LevelProvider::class.java
                || parameterContext.parameter.type == TestPlayer::class.java
                || parameterContext.parameter.type == TestPluginManager::class.java
                || parameterContext.parameter.type == Level::class.java
    }

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(parameterContext: ParameterContext, context: ExtensionContext): Any {
        if (parameterContext.parameter.type == GameMockExtension::class.java) {
            return gameMockExtension!!
        } else if (parameterContext.parameter.type == BlockRegistry::class.java) {
            return BLOCK_REGISTRY!!
        } else if (parameterContext.parameter.type == LevelProvider::class.java) {
            return level.getProvider()
        } else if (parameterContext.parameter.type == Level::class.java) {
            return level
        } else if (parameterContext.parameter.type == TestPlayer::class.java) {
            return player
        } else if (parameterContext.parameter.type == TestPluginManager::class.java) {
            return pluginManager!!
        }
        return super.resolveParameter(parameterContext, context)
    }

    fun stopNetworkTickLoop() {
        running.set(false)
    }

    fun mockNetworkTickLoop() {
        val main = Thread.currentThread()
        val t = Thread {
            while (running.get()) {
                try {
                    network!!.process()
                } catch (ignore: Exception) {
                }
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
            LockSupport.unpark(main)
        }
        t.isDaemon = true
        t.start()
        LockSupport.park()
    }

    companion object {
        var banList: BanList = Mockito.mock(BanList::class.java)
        var pluginManager: TestPluginManager? = null
        var simpleCommandMap: SimpleCommandMap = Mockito.mock(SimpleCommandMap::class.java)
        var serverScheduler: ServerScheduler? = null
        var network: Network? = null
        var level: Level

        val server: Server = Mockito.mock(Server::class.java)
        var gameMockExtension: GameMockExtension? = null
        var BLOCK_REGISTRY: BlockRegistry? = null
        var player: TestPlayer

        init {
            Mockito.mockStatic(Server::class.java).use { serverMockedStatic ->
                serverMockedStatic.`when`<Any> { Server.instance }.thenReturn(server)
                Registries.PACKET_DECODER.init()
                Registries.ENTITY.init()
                Profession.init()
                Registries.BLOCKENTITY.init()
                Registries.BLOCKSTATE_ITEMMETA.init()
                Registries.BLOCK.init()
                Enchantment.init()
                Registries.ITEM_RUNTIMEID.init()
                Registries.POTION.init()
                Registries.ITEM.init()
                Registries.CREATIVE.init()
                Registries.BIOME.init()
                Registries.FUEL.init()
                Registries.GENERATE_STAGE.init()
                Registries.GENERATOR.init()
                Registries.RECIPE.init()
                Registries.EFFECT.init()
                Attribute.init()
                BlockComposter.init()
                DispenseBehaviorRegister.init()
                BLOCK_REGISTRY = Registries.BLOCK

                serverScheduler = ServerScheduler()
                Mockito.`when`(server.scheduler).thenReturn(serverScheduler)
                Mockito.`when`(banList.entries).thenReturn(LinkedHashMap())
                Mockito.`when`(server.bannedIPs).thenReturn(banList)
                Mockito.`when`(server.lang).thenReturn(Lang("eng", "src/main/resources/language"))
                val serverSettings = ChorusTOML.load(File("chorus.toml"))
                Mockito.`when`(server.settings).thenReturn(serverSettings)
                Mockito.`when`(server.apiVersion).thenReturn("1.0.0")
                Mockito.`when`(simpleCommandMap.commands).thenReturn(emptyMap())

                pluginManager = TestPluginManager(server, simpleCommandMap)
                pluginManager!!.registerInterface(JavaPluginLoader::class.java)
                Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
                pluginManager!!.loadInternalPlugin()

                Mockito.`when`(server.motd).thenReturn("PNX")
                Mockito.`when`(server.onlinePlayers).thenReturn(HashMap())
                Mockito.`when`(server.gamemode).thenReturn(1)
                Mockito.`when`(server.name).thenReturn("PNX")
                Mockito.`when`(server.chorusVersion).thenReturn("1.0.0")
                Mockito.`when`(server.gitCommit).thenReturn("1.0.0")
                Mockito.`when`(server.maxPlayers).thenReturn(100)
                Mockito.`when`(server.hasWhitelist()).thenReturn(false)
                Mockito.`when`(server.port).thenReturn(19132)
                Mockito.`when`(server.ip).thenReturn("127.0.0.1")

                Mockito.`when`(server.network).thenCallRealMethod()
                Mockito.`when`(server.getAutoSave()).thenReturn(false)
                Mockito.`when`(server.tick).thenReturn(1)
                Mockito.`when`(server.viewDistance).thenReturn(4)
                Mockito.`when`(server.recipeRegistry).thenCallRealMethod()

                val pool = CoroutineScope(Dispatchers.Default)
                Mockito.`when`(server.computeScope).thenReturn(pool)
                Mockito.`when`(server.commandMap).thenReturn(simpleCommandMap)
                Mockito.`when`(server.scoreboardManager).thenReturn(null)
                try {
                    val positionTrackingService =
                        PositionTrackingService(File(Chorus.DATA_PATH, "services/position_tracking_db"))
                    Mockito.`when`(server.getPositionTrackingService()).thenReturn(positionTrackingService)
                } catch (e: FileNotFoundException) {
                    throw RuntimeException(e)
                }
                Mockito.doNothing().`when`(server).sendRecipeList(ArgumentMatchers.any())
                try {
                    FieldUtils.writeDeclaredField(server, "levelArray", Level.EMPTY_ARRAY, true)
                    FieldUtils.writeDeclaredField(server, "autoSave", false, true)
                    FieldUtils.writeDeclaredField(
                        server,
                        "tickAverage",
                        floatArrayOf(
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f
                        ),
                        true
                    )
                    FieldUtils.writeDeclaredField(
                        server,
                        "useAverage",
                        floatArrayOf(
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f,
                            20f
                        ),
                        true
                    )
                    network = Network(server)
                    FieldUtils.writeDeclaredField(server, "network", network, true)
                    FieldUtils.writeDeclaredStaticField(Server::class.java, "instance", server, true)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                }
            }
        }

        //mock player
        init {
            val serverSession = Mockito.mock(BedrockSession::class.java)
            val info = PlayerInfo(
                "test",
                UUID.randomUUID(),
                Skin(),
                Mockito.mock(ClientChainData::class.java)
            )
            val dataPacketManager = DataPacketManager()
            Mockito.`when`(serverSession.dataPacketManager).thenReturn(dataPacketManager)
            Mockito.doNothing().`when`(serverSession).sendPacketImmediately(ArgumentMatchers.any<DataPacket>())
            Mockito.doNothing().`when`(serverSession).sendPacketImmediately(ArgumentMatchers.any<Packet>())
            Mockito.doNothing().`when`(serverSession).sendPacket(ArgumentMatchers.any<DataPacket>())
            Mockito.doNothing().`when`(serverSession).sendPacket(ArgumentMatchers.any<Packet>())
            player = TestPlayer(serverSession, info)
            player.adventureSettings = AdventureSettings(player)
            player.loggedIn = true
            player.spawned = true
            TestUtils.setField(
                Player::class.java, player, "info", PlayerInfo(
                    "test", UUID.nameUUIDFromBytes(byteArrayOf(1, 2, 3)), Mockito.mock(
                        Skin::class.java
                    ), Mockito.mock(ClientChainData::class.java)
                )
            )
            player.setInventories(
                arrayOf<Inventory>(
                    HumanInventory(player),
                    HumanOffHandInventory(player),
                    HumanEnderChestInventory(player)
                )
            )
            player.addDefaultWindows()
            TestUtils.setField(Player::class.java, player, "foodData", PlayerFood(player, 20, 20f))
            try {
                FileUtils.copyDirectory(File("src/test/resources/level"), File("src/test/resources/newlevel"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            level = Level(
                "newlevel",
                "src/test/resources/newlevel",
                1,
                LevelDBProvider::class.java,
                GeneratorConfig(
                    "flat",
                    114514,
                    false,
                    LevelConfig.AntiXrayMode.LOW,
                    true,
                    DimensionEnum.OVERWORLD.dimensionData,
                    HashMap()
                )
            )
            level.initLevel()

            val map = HashMap<Int, Level>()
            map[1] = level
            Mockito.`when`(server.levels).thenReturn(map)

            val players: MutableMap<InetSocketAddress, Player?> = HashMap()
            players[InetSocketAddress("127.0.0.1", 63333)] = player
            TestUtils.setField(Server::class.java, server, "players", players)

            player.level = level
            player.setPosition(Vector3(0.0, 100.0, 0.0))

            val t = Thread {
                level.close()
                try {
                    val file1 = Path.of("services").toFile()
                    if (file1.exists()) {
                        FileUtils.deleteDirectory(file1)
                    }
                    val file2 = Path.of("src/test/resources/newlevel").toFile()
                    if (file2.exists()) {
                        FileUtils.deleteDirectory(file2)
                    }
                    val file3 = Path.of("config.yml").toFile()
                    if (file3.exists()) {
                        FileUtils.delete(file3)
                    }
                    println("TEST END!!!!!")
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            Runtime.getRuntime().addShutdownHook(t)

            gameMockExtension = GameMockExtension()
        }

        val running: AtomicBoolean = AtomicBoolean(true)
    }
}
