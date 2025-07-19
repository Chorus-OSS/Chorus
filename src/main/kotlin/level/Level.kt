package org.chorus_oss.chorus.level


import com.google.common.base.Preconditions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.customblock.CustomBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.Entity.Companion.getDefaultNBT
import org.chorus_oss.chorus.entity.EntityAsyncPrepare
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.*
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.weather.EntityLightningBolt
import org.chorus_oss.chorus.event.block.BlockBreakEvent
import org.chorus_oss.chorus.event.block.BlockPlaceEvent
import org.chorus_oss.chorus.event.block.BlockUpdateEvent
import org.chorus_oss.chorus.event.level.*
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.event.weather.LightningStrikeEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL_PRIORITY
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemBucket
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.format.ChunkState
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.format.LevelConfig.AntiXrayMode
import org.chorus_oss.chorus.level.format.LevelConfig.GeneratorConfig
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.level.generator.ChunkGenerateContext
import org.chorus_oss.chorus.level.generator.Generator
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.level.particle.Particle
import org.chorus_oss.chorus.level.tickingarea.TickingArea
import org.chorus_oss.chorus.level.util.SimpleTickCachedBlockStore
import org.chorus_oss.chorus.level.util.TickCachedBlockStore
import org.chorus_oss.chorus.level.vibration.SimpleVibrationManager
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationManager
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.*
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.scheduler.BlockUpdateScheduler
import org.chorus_oss.chorus.scheduler.ServerScheduler
import org.chorus_oss.chorus.utils.*
import org.chorus_oss.nbt.TagSerialization
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ChunkPos
import java.awt.Color
import java.io.File
import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


class Level(
    name: String,
    path: String,
    val dimensionCount: Int,
    provider: Class<out LevelProvider>,
    generatorConfig: GeneratorConfig
) : Loggable {
    val players = ConcurrentHashMap<Long, Player>()
    val entities = ConcurrentHashMap<Long, Entity>()
    val blockEntities = ConcurrentHashMap<Long, BlockEntity>()

    val updateEntities = ConcurrentHashMap<Long, Entity>()
    private val updateBlockEntities = ConcurrentLinkedQueue<BlockEntity>()

    private val chunkGenerationQueue = ConcurrentHashMap<Long, Boolean?>()
    private var chunkGenerationQueueSize = 8
    val id: Int = levelIdCounter++

    // Loaders still remain single-threaded
    private val loaders = HashMap<Int, ChunkLoader>()
    private val loaderCounter: MutableMap<Int, Int> = HashMap<Int, Int>()

    /*
     * <ChunkIndex,<ChunkLoader ID,ChunkLoader>>
     */
    private val chunkLoaders = HashMap<Long, MutableMap<Int, ChunkLoader>>()

    // Computation atomicity may be required in addChunkPacket(int, int, DataPacket)
    private val chunkPackets = ConcurrentHashMap<Long, Deque<DataPacket>>()

    private val unloadQueue = ConcurrentHashMap<Long, Long>()
    private val tickCachedBlocks = ConcurrentHashMap<Long, TickCachedBlockStore>()
    private val highLightChunks: MutableSet<Long> = HashSet()

    // Avoid OOM, gc'd references result in whole chunk being sent (possibly higher cpu)
    private val changedBlocks = HashMap<Long, SoftReference<MutableMap<Int, Any>>>()

    // Storing the vector is redundant
    private val changeBlocksPresent = Any()

    // Storing extra blocks past 512 is redundant
    private val changeBlocksFullMap: MutableMap<Int, Any> = object : HashMap<Int, Any>() {
        override val size: Int
            get() = Character.MAX_VALUE.code
    }

    private val updateQueue: BlockUpdateScheduler
    private val normalUpdateQueue: Queue<QueuedUpdate> = ConcurrentLinkedDeque()

    private val chunkSendQueue: ConcurrentHashMap<Long, ConcurrentHashMap<Int, Player>> = ConcurrentHashMap()

    private val chunkTickList: MutableMap<Long, Int> = HashMap()

    @JvmField
    val vibrationManager: VibrationManager = SimpleVibrationManager(this)
    var stopTime: Boolean = false
    var skyLightSubtracted: Int

    @JvmField
    var sleepTicks: Int = 0

    @JvmField
    var tickRateTime: Int = 0

    @JvmField
    var tickRateCounter: Int = 0

    /**
     * 当tps过低的时候，tps优化延迟会上升，计算密集型任务应当每隔此tick才运行一次
     */
    @JvmField
    var tickRateOptDelay: Int = 1

    lateinit var gameRules: GameRules
    private var provider: AtomicReference<LevelProvider>
    private var time: Float
    private var nextTimeSendTick = 0
    val name: String

    @JvmField
    val folderPath: String
    private val mutableBlock: Vector3? = null

    @JvmField
    var autoSave: Boolean = Server.instance.getAutoSave()
    private val temporalVector: Vector3
    private val chunkTickRadius: Int
    private val chunksPerTicks: Int
    private val clearChunksOnTick: Boolean
    var generator: Generator
    private val generatorClass = Registries.GENERATOR[generatorConfig.name]

    private var updateLCG: Int = ThreadLocalRandom.current().nextInt()
        get() = (((field * 3) xor LCG_CONSTANT).also { field = it })

    @JvmField
    var tickRate: Int
    var currentTick: Long = 0
        private set

    private val lightQueue: MutableMap<Long, MutableMap<Int, Any>> = ConcurrentHashMap(8, 0.9f, 1)

    /**base tick system */
    val baseTickThread: Thread


    val baseTickGameLoop: GameLoop

    /**sub tick system */
    val subTickThread: Thread
    val subTickGameLoop: GameLoop

    //Scheduler

    var scheduler: ServerScheduler

    /**antiXray system */
    var antiXraySystem: AntiXraySystem? = null
        private set

    /**weather system */
    var isRaining: Boolean = false
        private set
    var rainTime: Int = 0
    private var thundering = false
    var thunderTime: Int = 0

    private val playerWeatherShowMap = HashMap<String, Int>()

    var isAntiXrayEnabled: Boolean
        /**
         * Is anti-xray enabled.
         */
        get() = this.antiXraySystem != null
        /**
         * enable the anti-xray system.
         */
        set(antiXrayEnabled) {
            if (antiXrayEnabled) {
                if (antiXraySystem == null) {
                    this.antiXraySystem = AntiXraySystem(this)
                }
            } else {
                this.antiXraySystem = null
            }
        }

    val tick: Int
        get() = if (Server.instance.settings.levelSettings.levelThread) this.baseTickGameLoop.getTick() else Server.instance.tick

    fun recalcTickOptDelay(): Int {
        return if (tickRateTime > 40) {
            (tickRateOptDelay shl 1).coerceAtMost(8)
        } else if (tickRateOptDelay == 1) {
            1
        } else {
            tickRateOptDelay shr 1
        }
    }

    fun isHighLightChunk(chunkX: Int, chunkZ: Int): Boolean {
        return highLightChunks.contains(chunkHash(chunkX, chunkZ))
    }

    fun initLevel() {
        this.gameRules = requireProvider().gamerules
        val spawn = this.spawnLocation
        if (!getChunk(spawn.position.chunkX, spawn.position.chunkZ, true).chunkState.canSend()) {
            this.generateChunk(spawn.position.chunkX, spawn.position.chunkZ)
        }
        subTickThread.start()
        if (Server.instance.settings.levelSettings.levelThread) {
            baseTickThread.start()
        }
        log.info(
            Server.instance.lang.tr(
                "chorus.level.init",
                TextFormat.GREEN.toString() + this.name + TextFormat.RESET
            )
        )
    }

    fun getProvider(): LevelProvider {
        return provider.get()
    }

    /**
     * Returns the level provider if it exists. Tries to close and unregister the level and then throw an exception if it doesn't.
     *
     * @throws LevelException If the level is already closed
     */
    fun requireProvider(): LevelProvider {
        val levelProvider = getProvider()
        if (levelProvider == null) {
            val levelException = LevelException("The level \"$folderPath\" is already closed (have no providers)")
            try {
                close()
            } catch (e: Exception) {
                levelException.addSuppressed(e)
            }
            throw levelException
        }
        return levelProvider
    }

    fun close() {
        if (Server.instance.settings.levelSettings.levelThread && baseTickThread.isAlive) {
            baseTickGameLoop.stop()
        } else remove()
    }

    private fun remove() {
        subTickGameLoop.stop()
        scheduler.cancelAllTasks()
        scheduler.mainThreadHeartbeat(this.tick + 10000)
        Server.instance.levels.remove(this.id)
        val levelProvider = provider.get()
        if (levelProvider != null) {
            if (this.autoSave) {
                this.save(true)
            }
            levelProvider.close()
        }
        provider.set(null)
    }

    fun addSound(pos: Vector3, sound: Sound) {
        this.addSound(pos, sound, 1f, 1f, *arrayOf())
    }

    fun addSound(pos: Vector3, sound: Sound, volume: Float, pitch: Float) {
        this.addSound(pos, sound, volume, pitch, *arrayOf())
    }

    fun addSound(pos: Vector3, sound: Sound, volume: Float, pitch: Float, players: Collection<Player>) {
        this.addSound(pos, sound, volume, pitch, *players.toTypedArray())
    }

    fun addSound(pos: Vector3, sound: Sound, volume: Float, pitch: Float, vararg players: Player) {
        Preconditions.checkArgument(volume in 0.0..1.0, "Sound volume must be between 0 and 1")
        Preconditions.checkArgument(pitch >= 0, "Sound pitch must be higher than 0")

        val packet = org.chorus_oss.protocol.packets.PlaySoundPacket(
            soundName = sound.sound,
            position = org.chorus_oss.protocol.types.Vector3f(pos),
            volume = volume,
            pitch = pitch,
        )

        if (players.isEmpty()) {
            addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, packet)
        } else {
            Server.broadcastPacket(players.toList(), packet)
        }
    }

    @JvmOverloads
    fun addLevelEvent(type: Int, data: Int, pos: Vector3? = null) {
        if (pos == null) {
            addLevelEvent(type, data, 0f, 0f, 0f)
        } else {
            addLevelEvent(type, data, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
        }
    }

    fun addLevelEvent(type: Int, data: Int, x: Float, y: Float, z: Float) {
        val packet = LevelEventPacket()
        packet.evid = type
        packet.x = x
        packet.y = y
        packet.z = z
        packet.data = data

        this.addChunkPacket(floor(x).toInt() shr 4, floor(z).toInt() shr 4, packet)
    }

    @JvmOverloads
    fun addLevelEvent(pos: Vector3, event: Int, data: Int = 0) {
        val pk = LevelEventPacket()
        pk.evid = event
        pk.x = pos.x.toFloat()
        pk.y = pos.y.toFloat()
        pk.z = pos.z.toFloat()
        pk.data = data

        addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
    }

    fun addLevelEvent(pos: Vector3, event: Int, data: CompoundTag) {
        val pk = org.chorus_oss.protocol.packets.LevelEventGenericPacket(
            eventID = event,
            serializedEventData = Buffer().apply {
                org.chorus_oss.nbt.Tag.serialize(
                    org.chorus_oss.nbt.tags.CompoundTag(data),
                    this,
                    TagSerialization.NetLE,
                    false
                )
            }.readByteString()
        )
        this.addChunkPacket(pos.chunkX, pos.chunkZ, pk)
    }

    @JvmOverloads
    fun addLevelSoundEvent(
        pos: Vector3,
        type: org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType,
        data: Int,
        entityType: Int,
        isBaby: Boolean = false,
        isGlobal: Boolean = false
    ) {
        val identifier = Registries.ENTITY.getEntityIdentifier(entityType)
        addLevelSoundEvent(pos, type, data, identifier, isBaby, isGlobal)
    }

    /**
     * Broadcasts a LevelSound to players,use LevelSoundEventPacket
     *
     * @param pos        the pos
     * @param type       the sound type id,get from[org.chorus_oss.protocol.packets.LevelSoundEventPacket]
     * @param data       the extra data,default -1
     * @param identifier the identifier,default ":"
     * @param isBaby     the is baby,default false
     * @param isGlobal   the is global,default false
     */
    /**
     * Broadcasts sound to players
     *
     * @param pos  position where sound should be played
     * @param type ID of the sound from [org.chorus_oss.protocol.packetsLevelSoundEventPacket]
     * @param data generic data that can affect sound
     */
    @JvmOverloads
    fun addLevelSoundEvent(
        pos: Vector3,
        type: org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType,
        data: Int = -1,
        identifier: String = ":",
        isBaby: Boolean = false,
        isGlobal: Boolean = false
    ) {
        val pk = org.chorus_oss.protocol.packets.LevelSoundEventPacket(
            soundType = type,
            position = org.chorus_oss.protocol.types.Vector3f(pos),
            extraData = data,
            entityType = identifier,
            babyMob = isBaby,
            global = isGlobal,
            actorUniqueID = -1,
        )
        this.addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
    }

    fun addParticle(particle: Particle, player: Player) {
        this.addParticle(particle, arrayOf(player))
    }

    fun addParticle(particle: Particle, players: Collection<Player>) {
        this.addParticle(particle, players.toTypedArray())
    }

    @JvmOverloads
    fun addParticle(particle: Particle, players: Array<Player>? = null) {
        val packets = particle.encode()

        if (players == null) {
            for (packet in packets) {
                this.addChunkPacket(particle.x.toInt() shr 4, particle.z.toInt() shr 4, packet)
            }
        } else {
            for (p in packets) {
                Server.broadcastPacket(players, p)
            }
        }
    }

    fun addParticleEffect(pos: Vector3, particleEffect: ParticleEffect) {
        this.addParticleEffect(pos, particleEffect, -1, this.dimension, *emptyArray())
    }

    fun addParticleEffect(pos: Vector3, particleEffect: ParticleEffect, uniqueEntityId: Long) {
        this.addParticleEffect(pos, particleEffect, uniqueEntityId, this.dimension, *emptyArray())
    }

    fun addParticleEffect(pos: Vector3, particleEffect: ParticleEffect, uniqueEntityId: Long, dimensionId: Int) {
        this.addParticleEffect(pos, particleEffect, uniqueEntityId, dimensionId, *emptyArray())
    }

    fun addParticleEffect(
        pos: Vector3,
        particleEffect: ParticleEffect,
        uniqueEntityId: Long,
        dimensionId: Int,
        players: Collection<Player>
    ) {
        this.addParticleEffect(
            pos,
            particleEffect,
            uniqueEntityId,
            dimensionId,
            *players.toTypedArray()
        )
    }

    fun addParticleEffect(
        pos: Vector3,
        particleEffect: ParticleEffect,
        uniqueEntityId: Long,
        dimensionId: Int,
        vararg players: Player
    ) {
        this.addParticleEffect(pos.asVector3f(), particleEffect.identifier, uniqueEntityId, dimensionId, *players)
    }

    fun addParticleEffect(
        pos: Vector3f,
        identifier: String,
        uniqueEntityId: Long,
        dimensionId: Int,
        vararg players: Player
    ) {
        val pk = org.chorus_oss.protocol.packets.SpawnParticleEffectPacket(
            dimension = dimensionId.toByte(),
            entityUniqueID = uniqueEntityId,
            position = org.chorus_oss.protocol.types.Vector3f(pos),
            identifier = identifier,
            moLangVariablesJSON = null
        )

        if (players.isEmpty()) {
            addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
        } else {
            Server.broadcastPacket(players.toList(), pk)
        }
    }

    @JvmOverloads
    fun unload(force: Boolean = false): Boolean {
        val ev: LevelUnloadEvent = LevelUnloadEvent(this)

        if (this === Server.instance.defaultLevel && !force) {
            ev.cancelled = true
        }

        Server.instance.pluginManager.callEvent(ev)

        if (!force && ev.cancelled) {
            return false
        }

        log.info(
            Server.instance.lang.tr(
                "chorus.level.unloading",
                TextFormat.GREEN.toString() + this.getLevelName() + TextFormat.WHITE
            )
        )
        val defaultLevel = Server.instance.defaultLevel

        for (player in getPlayers().values) {
            if (this === defaultLevel || defaultLevel == null) {
                player.close(player.leaveMessage, "Forced default level unload")
            } else {
                player.teleport(Server.instance.defaultLevel!!.safeSpawn)
            }
        }

        this.close()
        if (force && Server.instance.settings.levelSettings.levelThread) {
            Server.instance.scheduler.scheduleDelayedTask({
                if (baseTickThread.isAlive) {
                    log.error(getLevelName() + " failed to unload. Trying to stop the thread.")
                    baseTickThread.interrupt()
                }
            }, 100)
        }
        return true
    }

    fun getChunkPlayers(chunkX: Int, chunkZ: Int): MutableMap<Int, Player> {
        val index = chunkHash(chunkX, chunkZ)
        if (chunkLoaders.containsKey(index)) {
            return chunkLoaders[index]!!.entries
                .stream()
                .filter { it.value is Player }
                .collect(
                    { HashMap() },
                    { m, e ->
                        m[e.key] = e.value as Player
                    },
                    { obj, m ->
                        obj.putAll(
                            m!!
                        )
                    })
        }
        return mutableMapOf()
    }

    fun getChunkLoaders(chunkX: Int, chunkZ: Int): Array<ChunkLoader> {
        val index = chunkHash(chunkX, chunkZ)
        return if (chunkLoaders.containsKey(index)) {
            chunkLoaders[index]!!.values.toTypedArray()
        } else {
            ChunkLoader.EMPTY_ARRAY
        }
    }

    fun addChunkPacket(chunkX: Int, chunkZ: Int, packet: DataPacket?) {
        val index = chunkHash(chunkX, chunkZ)
        val packets = chunkPackets.computeIfAbsent(
            index
        ) { ConcurrentLinkedDeque<DataPacket>() }
        packets.add(packet)
    }

    fun addChunkPacket(x: Int, z: Int, packet: Packet) {
        addChunkPacket(x, z, MigrationPacket(packet))
    }

    @JvmOverloads
    fun registerChunkLoader(loader: ChunkLoader, chunkX: Int, chunkZ: Int, autoLoad: Boolean = true) {
        val hash = loader.loaderId
        val index = chunkHash(chunkX, chunkZ)
        if (!chunkLoaders.containsKey(index)) {
            chunkLoaders[index] = HashMap()
        } else if (chunkLoaders[index]!!.containsKey(hash)) {
            return
        }

        chunkLoaders[index]!![hash] = loader

        if (!loaders.containsKey(hash)) {
            loaderCounter[hash] = 1
            loaders[hash] = loader
        } else {
            loaderCounter[hash] = loaderCounter[hash]!! + 1
        }

        this.cancelUnloadChunkRequest(hash.toLong())

        if (autoLoad) {
            this.loadChunk(chunkX, chunkZ)
        }
    }

    @JvmOverloads
    fun unregisterChunkLoader(loader: ChunkLoader, chunkX: Int, chunkZ: Int, isSafeUnload: Boolean = true): Boolean {
        val loaderId = loader.loaderId
        val chunkHash = chunkHash(chunkX, chunkZ)
        val chunkLoadersIndex: MutableMap<Int, ChunkLoader>? = chunkLoaders[chunkHash]
        if (chunkLoadersIndex != null) {
            val oldLoader = chunkLoadersIndex.remove(loaderId)
            if (oldLoader != null) {
                if (chunkLoadersIndex.isEmpty()) {
                    chunkLoaders.remove(chunkHash)
                    return this.unloadChunkRequest(chunkX, chunkZ, isSafeUnload)
                }

                var count = loaderCounter[loaderId]!!
                if (--count == 0) {
                    loaderCounter.remove(loaderId)
                    loaders.remove(loaderId)
                } else {
                    loaderCounter[loaderId] = count
                }
                return true
            }
            return false
        }
        return false
    }

    fun checkTime() {
        if (!this.stopTime && gameRules.getBoolean(GameRule.DO_DAYLIGHT_CYCLE)) {
            this.time += tickRate.toFloat()
        }
    }

    fun sendTime(vararg players: Player) {
        val pk = org.chorus_oss.protocol.packets.SetTimePacket(
            time = time.toInt()
        )
        Server.broadcastPacket(players.toList(), pk)
    }

    fun sendTime() {
        this.sendTime(*players.values.toTypedArray())
    }

    fun releaseTickCachedBlocks() {
        synchronized(this.tickCachedBlocks) {
            for (each in tickCachedBlocks.values) {
                each.clearCachedStore()
            }
        }
    }

    private fun doTick(gameLoop: GameLoop) {
        val baseTickRate: Int = Server.instance.settings.levelSettings.baseTickRate
        val levelTime = System.currentTimeMillis()
        val tickMs = (System.currentTimeMillis() - levelTime).toInt()
        doTick(gameLoop.getTick())
        if (Server.instance.settings.levelSettings.autoTickRate) {
            if (tickMs < 50 && this.tickRate > baseTickRate) {
                val r: Int
                this.tickRate = (this.tickRate - 1).also { r = it }
                if (r > baseTickRate) {
                    this.tickRateCounter = this.tickRate
                }
                log.debug(
                    "Raising level \"{}\" tick rate to {} ticks", this.getLevelName(),
                    tickRate
                )
            } else if (tickMs >= 50) {
                val autoTickRateLimit: Int = Server.instance.settings.levelSettings.autoTickRateLimit
                if (this.tickRate == baseTickRate) {
                    this.tickRate = (baseTickRate + 1).coerceAtLeast(autoTickRateLimit.coerceAtMost(tickMs / 50))
                    log.debug(
                        "Level \"{}\" took {}ms, setting tick rate to {} ticks",
                        this.getLevelName(), round(tickMs.toDouble(), 2),
                        tickRate
                    )
                } else if ((tickMs / this.tickRate) >= 50 && this.tickRate < autoTickRateLimit) {
                    this.tickRate += 1
                    log.debug(
                        "Level \"{}\" took {}ms, setting tick rate to {} ticks",
                        this.getLevelName(), round(tickMs.toDouble(), 2),
                        tickRate
                    )
                }
                this.tickRateCounter = this.tickRate
            }
        }
    }

    fun doTick(currentTick: Int) {
        players.values.forEach(Consumer { player: Player -> player.session.tick() })
        requireProvider()
        try {
            scheduler.mainThreadHeartbeat(currentTick)
            updateBlockLight(lightQueue)
            this.checkTime()
            if (currentTick >= nextTimeSendTick) { // Send time to client every 30 seconds to make sure it
                this.sendTime()
                nextTimeSendTick = currentTick + 30 * 20
            }

            // 检查突出区块（玩家附近3x3区块）
            if ((currentTick and 127) == 0) { // 每127刻检查一次是比较合理的
                highLightChunks.clear()
                for (player in players.values) {
                    if (player.isOnline) {
                        val chunkX = player.position.chunkX
                        val chunkZ = player.position.chunkZ
                        for (dx in -1..1) {
                            for (dz in -1..1) {
                                highLightChunks.add(chunkHash(chunkX + dx, chunkZ + dz))
                            }
                        }
                    }
                }
            }
            checkWeather()

            this.skyLightSubtracted = this.calculateSkylightSubtracted(1f)

            this.currentTick++

            updateQueue.tick(this.currentTick)

            while (!normalUpdateQueue.isEmpty()) {
                val queuedUpdate = normalUpdateQueue.poll()
                val block = getBlock(queuedUpdate.block.position, queuedUpdate.block.layer)
                val event: BlockUpdateEvent = BlockUpdateEvent(block)
                Server.instance.pluginManager.callEvent(event)

                if (!event.cancelled) {
                    block.onUpdate(BLOCK_UPDATE_NORMAL)
                    if (queuedUpdate.neighbor != null) {
                        block.onNeighborChange(queuedUpdate.neighbor.getOpposite())
                    }
                }
            }
            if (!updateEntities.isEmpty()) {
                runBlocking {
                    updateEntities.values.map { e ->
                        Server.instance.computeScope.async {
                            if (e.isInitialized() && e is EntityAsyncPrepare) {
                                e.asyncPrepare(tick)
                            }
                        }
                    }.awaitAll()
                }

                for (id in updateEntities.keys) {
                    val entity = updateEntities[id]
                    if (entity == null) {
                        updateEntities.remove(id)
                        continue
                    }
                    if (entity.closed || !entity.onUpdate(currentTick)) {
                        updateEntities.remove(id)
                    }
                }
            }
            updateBlockEntities.removeIf { blockEntity: BlockEntity -> !(!blockEntity.closed && blockEntity.onUpdate()) }

            this.tickChunks()
            synchronized(changedBlocks) {
                if (changedBlocks.isNotEmpty()) {
                    if (!players.isEmpty()) {
                        val iter = changedBlocks.entries.iterator()
                        while (iter.hasNext()) {
                            val entry = iter.next()
                            val index = entry.key
                            val blocks = entry.value.get()
                            val chunkX = getHashX(index)
                            val chunkZ = getHashZ(index)
                            if (blocks == null || blocks.size > MAX_BLOCK_CACHE) {
                                val chunk = this.getChunk(chunkX, chunkZ)
                                chunk.reObfuscateChunk()
                                for (p in getChunkPlayers(chunkX, chunkZ).values) {
                                    p.onChunkChanged(chunk)
                                }
                            } else {
                                val toSend: Collection<Player> =
                                    getChunkPlayers(chunkX, chunkZ).values
                                val playerArray = toSend.toTypedArray()
                                val size: Int = blocks.size
                                if (isAntiXrayEnabled) {
                                    antiXraySystem!!.obfuscateSendBlocks(index, playerArray, blocks)
                                } else {
                                    val blocksArray = arrayOfNulls<Vector3>(size)
                                    var i = 0
                                    for (blockHash in blocks.keys) {
                                        val hash = getBlockXYZ(index, blockHash, this)
                                        blocksArray[i++] = hash
                                    }
                                    this.sendBlocks(
                                        playerArray,
                                        blocksArray,
                                        org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL
                                    )
                                }
                            }
                        }
                    }
                    changedBlocks.clear()
                }
            }
            if (this.sleepTicks > 0 && --this.sleepTicks <= 0) {
                this.checkSleep()
            }

            for (index in chunkPackets.keys) {
                val chunkX = getHashX(index)
                val chunkZ = getHashZ(index)
                val chunkPlayers: Array<Player> =
                    getChunkPlayers(chunkX, chunkZ).values.toTypedArray()
                if (chunkPlayers.isNotEmpty()) {
                    for (pk in chunkPackets[index]!!) {
                        Server.broadcastPacket(chunkPlayers, pk)
                    }
                }
            }
            chunkPackets.clear()

            if (gameRules.isStale) {
                val packet = org.chorus_oss.protocol.packets.GameRulesChangedPacket(
                    gameRules = gameRules.getGameRules().map { org.chorus_oss.protocol.types.GameRule(it.toPair()) }
                )
                Server.broadcastPacket(players.values, packet)
                gameRules.refresh()
            }
        } catch (e: Exception) {
            log.error(
                Server.instance.lang.tr(
                    "chorus.level.tickError",
                    this.folderPath, Utils.getExceptionMessage(e)
                ), e
            )
            e.printStackTrace()
        } finally {
            getPlayers().values.forEach(Consumer { obj: Player -> obj.checkNetwork() })
            releaseTickCachedBlocks()
        }
    }

    private fun checkWeather() {
        if (gameRules.getBoolean(GameRule.DO_WEATHER_CYCLE)) {
            for (entry in playerWeatherShowMap.entries) {
                val intValue = entry.value
                val key: String = entry.key
                if (intValue == 0) {
                    val player = Server.instance.getPlayer(key)
                    if (player != null) {
                        if (isRaining) {
                            val pk = LevelEventPacket()
                            pk.evid = LevelEventPacket.EVENT_START_RAINING
                            pk.data = rainTime
                            player.dataPacket(pk)
                            playerWeatherShowMap.put(key, 1)
                            if (isThundering()) {
                                val pk2 = LevelEventPacket()
                                pk2.evid = LevelEventPacket.EVENT_START_THUNDERSTORM
                                pk2.data = thunderTime
                                player.dataPacket(pk)
                                playerWeatherShowMap.put(key, 2)
                            }
                        }
                    }
                }
            }
            // Tick Weather
            if (this.dimension != DIMENSION_NETHER && this.dimension != DIMENSION_THE_END) {
                if (dayTime == tickRate) {
                    setRaining(false)
                    setThundering(false)
                }

                rainTime--
                if (this.rainTime <= 0) {
                    if (!this.setRaining(!this.isRaining)) { //if raining,set false
                        setRaining(!isRaining) // and if event cancel,revert raining change
                    }
                }

                thunderTime--
                if (this.thunderTime <= 0) {
                    if (!this.setThundering(!this.thundering)) {
                        setThundering(!thundering)
                    }
                }

                if (this.isThundering()) {
                    chunks
                    for (entry in this.chunks.entries) {
                        performThunder(entry.key, entry.value)
                    }
                }
            }
        } else {
            if (isRaining) {
                setRaining(false)
            }
            if (isThundering()) {
                setThundering(false)
            }
        }
    }

    /**
     * Spawn lightning when thunder
     */
    private fun performThunder(index: Long, chunk: IChunk) {
        if (areNeighboringChunksLoaded(index)) return
        if (ThreadLocalRandom.current().nextInt(100000) == 0) {
            val LCG = this.updateLCG shr 2

            val chunkX = chunk.x * 16
            val chunkZ = chunk.z * 16
            val vector = this.adjustPosToNearbyEntity(
                Vector3(
                    (chunkX + (LCG and 0xf)).toDouble(),
                    0.0,
                    (chunkZ + (LCG shr 8 and 0xf)).toDouble()
                )
            )

            val biome = this.getBiomeId(vector.floorX, 70, vector.floorZ)
            if (Registries.BIOME[biome]!!.data.rain) {
                return
            }

            val b = this.getBlock(vector.floorX, vector.floorY, vector.floorZ)
            if (b.properties != BlockTallGrass.properties && b !is BlockFlowingWater) vector.y += 1.0
            val nbt = CompoundTag()
                .putList(
                    "Pos", ListTag<FloatTag>().add(FloatTag(vector.x))
                        .add(FloatTag(vector.y)).add(FloatTag(vector.z))
                )
                .putList(
                    "Motion", ListTag<FloatTag>().add(FloatTag(0f))
                        .add(FloatTag(0f)).add(FloatTag(0f))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>().add(FloatTag(0f))
                        .add(FloatTag(0f))
                )

            val bolt: EntityLightningBolt = EntityLightningBolt(chunk, nbt)
            val ev: LightningStrikeEvent = LightningStrikeEvent(this, bolt)
            Server.instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                bolt.spawnToAll()
            } else {
                bolt.isEffect = (false)
            }

            this.addLevelSoundEvent(
                vector,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Thunder,
                -1,
                Registries.ENTITY.getEntityNetworkId(EntityID.LIGHTNING_BOLT)
            )
            this.addLevelSoundEvent(
                vector,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Explode,
                -1,
                Registries.ENTITY.getEntityNetworkId(EntityID.LIGHTNING_BOLT)
            )
        }
    }

    fun adjustPosToNearbyEntity(pos: Vector3): Vector3 {
        var pos = pos
        pos.y = getHighestBlockAt(pos.floorX, pos.floorZ).toDouble()
        val axisalignedbb: AxisAlignedBB = SimpleAxisAlignedBB(
            pos.x,
            pos.y,
            pos.z,
            pos.x,
            (if (isOverWorld) 320 else 255).toDouble(),
            pos.z
        ).expand(3.0, 3.0, 3.0)
        val list: MutableList<Entity> = ArrayList()

        for (entity in this.getCollidingEntities(axisalignedbb)) {
            if (entity.isAlive() && canBlockSeeSky(entity.position)) {
                list.add(entity)
            }
        }

        if (!list.isEmpty()) {
            return list[ThreadLocalRandom.current().nextInt(list.size)].locator.position
        } else {
            if (pos.y == -1.0) {
                pos = pos.up(2)
            }

            return pos
        }
    }

    fun checkSleep() {
        if (players.isEmpty()) {
            return
        }

        var playerCount = 0
        var sleepingPlayerCount = 0
        for (p in getPlayers().values) {
            playerCount++
            if (p.isSleeping()) {
                sleepingPlayerCount++
            }
        }

        if (playerCount > 0 && sleepingPlayerCount / playerCount * 100 >= gameRules.getInteger(GameRule.PLAYERS_SLEEPING_PERCENTAGE)) {
            val time = this.getTime() % TIME_FULL

            if (time >= TIME_NIGHT && time < TIME_SUNRISE) {
                this.setTime(this.getTime() + TIME_FULL - time)

                for (p in getPlayers().values) {
                    p.stopSleep()
                }
            }
        }
    }

    @JvmOverloads
    fun sendBlockExtraData(
        x: Int,
        y: Int,
        z: Int,
        id: Int,
        data: Int,
        players: Collection<Player> = getChunkPlayers(x shr 4, z shr 4).values
    ) {
        sendBlockExtraData(x, y, z, id, data, players.toTypedArray())
    }

    fun sendBlockExtraData(x: Int, y: Int, z: Int, id: Int, data: Int, players: Array<Player>) {
        val pk = LevelEventPacket()
        pk.evid = LevelEventPacket.EVENT_SET_DATA
        pk.x = x + 0.5f
        pk.y = y + 0.5f
        pk.z = z + 0.5f
        pk.data = (data shl 8) or id

        Server.broadcastPacket(players, pk)
    }

    fun sendBlocks(target: Array<Player>, blocks: Array<out IVector3>) {
        this.sendBlocks(target, blocks, 0u, 0)
        this.sendBlocks(target, blocks, 0u, 1)
    }

    fun sendBlocks(target: Array<Player>, blocks: Array<out IVector3?>, flags: UInt) {
        this.sendBlocks(target, blocks, flags, 0)
        this.sendBlocks(target, blocks, flags, 1)
    }

    fun sendBlocks(target: Array<Player>, blocks: Array<out IVector3?>, flags: UInt, optimizeRebuilds: Boolean) {
        this.sendBlocks(target, blocks, flags, 0, optimizeRebuilds)
        this.sendBlocks(target, blocks, flags, 1, optimizeRebuilds)
    }

    @JvmOverloads
    fun sendBlocks(
        target: Array<Player>,
        blocks: Array<out IVector3?>,
        flags: UInt,
        dataLayer: Int,
        optimizeRebuilds: Boolean = false
    ) {
        var size = 0
        for (block in blocks) {
            if (block != null) size++
        }
        val packets: MutableList<org.chorus_oss.protocol.packets.UpdateBlockPacket> = mutableListOf()
        var chunks: MutableSet<Long>? = null
        if (optimizeRebuilds) {
            chunks = mutableSetOf()
        }
        for (b in blocks) {
            if (b == null) {
                continue
            }
            var first = !optimizeRebuilds

            val pos: Vector3 = b.vector3
            if (optimizeRebuilds) {
                val index = chunkHash(pos.chunkX, pos.chunkZ)
                if (!chunks!!.contains(index)) {
                    chunks.add(index)
                    first = true
                }
            }

            val bPos = BlockPos(pos)
            val updateBlockPacket = org.chorus_oss.protocol.packets.UpdateBlockPacket(
                position = bPos,
                newBlockRuntimeID = run {
                    if (b is Block) {
                        b.runtimeId.toUInt()
                    } else if (b is Vector3WithRuntimeId) {
                        if (dataLayer == 0) {
                            b.runtimeIdLayer0.toUInt()
                        } else {
                            b.runtimeIdLayer1.toUInt()
                        }
                    } else {
                        val hash = getBlockRuntimeId(bPos.x, bPos.y, bPos.z, dataLayer)
                        if (hash == Int.MIN_VALUE) {
                            continue
                        }
                        hash.toUInt()
                    }
                },
                flags = if (first) flags else 0u,
                layer = dataLayer.toUInt(),
            )
            packets.add(updateBlockPacket)
        }
        for (p in packets) {
            Server.broadcastPacket(target.toList(), p)
        }
    }

    private fun tickChunks() {
        if (this.chunksPerTicks <= 0 || loaders.isEmpty()) {
            chunkTickList.clear()
            return
        }

        val chunksPerLoader = 200.coerceAtMost(
            1.coerceAtLeast((((this.chunksPerTicks - loaders.size).toDouble() / loaders.size + 0.5)).toInt())
        )
        var randRange = 3 + chunksPerLoader / 30
        randRange = randRange.coerceAtMost(this.chunkTickRadius)

        val random = ThreadLocalRandom.current()
        if (!loaders.isEmpty()) {
            for (loader in loaders.values) {
                val chunkX = loader.locator.position.chunkX
                val chunkZ = loader.locator.position.chunkZ

                val index = chunkHash(chunkX, chunkZ)
                val existingLoaders = 0.coerceAtLeast(chunkTickList.getOrDefault(index, 0))
                chunkTickList.put(index, existingLoaders + 1)
                for (chunk in 0..<chunksPerLoader) {
                    val dx = random.nextInt(2 * randRange) - randRange
                    val dz = random.nextInt(2 * randRange) - randRange
                    val hash = chunkHash(dx + chunkX, dz + chunkZ)
                    if (!chunkTickList.containsKey(hash) && requireProvider().isChunkLoaded(hash)) {
                        chunkTickList.put(hash, -1)
                    }
                }
            }
        }

        if (!chunkTickList.isEmpty()) {
            val iter = chunkTickList.entries.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                val index = entry.key
                if (!areNeighboringChunksLoaded(index)) {
                    iter.remove()
                    continue
                }

                val loaders = entry.value

                val chunkX = getHashX(index)
                val chunkZ = getHashZ(index)

                val chunk: IChunk
                if ((getChunk(chunkX, chunkZ, false).also { chunk = it }) == null) {
                    iter.remove()
                    continue
                } else if (loaders <= 0) {
                    iter.remove()
                }

                for (entity in chunk.entities.values) {
                    entity.scheduleUpdate()
                }
                val tickSpeed = gameRules.getInteger(GameRule.RANDOM_TICK_SPEED)
                if (tickSpeed <= 0) {
                    continue
                }

                for (section in chunk.getSectionsSafe()) {
                    if (section == null || section.isEmpty) {
                        continue
                    }
                    for (i in 0..<tickSpeed) {
                        val lcg = this.updateLCG
                        val x = lcg and 0x0f
                        val y = lcg ushr 8 and 0x0f
                        val z = lcg ushr 16 and 0x0f
                        val state = section.getBlockState(x, y, z)
                        if (state != null && randomTickBlocks.contains(state.identifier)) {
                            val block = Block.get(
                                state,
                                this, (chunk.x shl 4) + x, (section.y.toInt() shl 4) + y, (chunk.z shl 4) + z
                            )
                            block.setLevel(this)
                            block.onUpdate(BLOCK_UPDATE_RANDOM)
                        }
                    }
                }
            }
        }

        if (this.clearChunksOnTick) {
            chunkTickList.clear()
        }
    }

    @JvmOverloads
    fun save(force: Boolean = false): Boolean {
        if (!this.autoSave && !force) {
            return false
        }

        Server.instance.pluginManager.callEvent(LevelSaveEvent(this))

        val levelProvider = this.requireProvider()
        levelProvider.time = time.toInt().toLong()
        levelProvider.rainTime = rainTime
        levelProvider.thunderTime = thunderTime
        levelProvider.currentTick = currentTick
        levelProvider.setGameRules(this.gameRules)
        this.saveChunks()
        levelProvider.saveLevelData()
        return true
    }

    fun saveChunks() {
        requireProvider().saveChunks()
    }

    fun updateComparatorOutputLevel(v: Vector3) {
        updateComparatorOutputLevelSelective(v, true)
    }

    fun updateComparatorOutputLevelSelective(v: Vector3, observer: Boolean) {
        for (face in BlockFace.Plane.HORIZONTAL_FACES) {
            temporalVector.setComponentsAdding(v, face)

            if (!this.isChunkLoaded(temporalVector.x.toInt() shr 4, temporalVector.z.toInt() shr 4)) {
                continue
            }
            var block1 = this.getBlock(temporalVector)

            if (BlockID.OBSERVER == block1.id) {
                if (observer) {
                    block1.onNeighborChange(face.getOpposite())
                }
            } else if (BlockRedstoneDiode.isDiode(block1)) {
                block1.onUpdate(BLOCK_UPDATE_REDSTONE)
            } else if (block1.isNormalBlock) {
                block1 = this.getBlock(temporalVector.setComponentsAdding(temporalVector, face))

                if (BlockRedstoneDiode.isDiode(block1)) {
                    block1.onUpdate(BLOCK_UPDATE_REDSTONE)
                }
            }
        }

        if (!observer) {
            return
        }

        for (face in BlockFace.Plane.VERTICAL_FACES) {
            val block1 = this.getBlock(temporalVector.setComponentsAdding(v, face))

            if (BlockID.OBSERVER == block1.id) {
                block1.onNeighborChange(face.getOpposite())
            }
        }
    }

    fun updateAround(pos: Vector3) {
        val block = getBlock(pos)
        for (face in BlockFace.entries) {
            val side = block.getSideAtLayer(0, face)
            normalUpdateQueue.add(QueuedUpdate(side, face))
            normalUpdateQueue.add(QueuedUpdate(side.getLevelBlockAtLayer(1), face))
        }
    }

    fun neighborChangeAroundImmediately(x: Int, y: Int, z: Int) {
        neighborChangeAroundImmediately(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
    }

    /**
     * 立即对围绕指定位置的方块发送neighborChange更新
     *
     * @param pos 指定位置
     */
    fun neighborChangeAroundImmediately(pos: Vector3) {
        for (face in BlockFace.entries) {
            val neighborBlock = getBlock(pos.getSide(face))
            neighborBlock.onNeighborChange(face.getOpposite())
        }
    }

    fun updateAroundObserver(pos: Vector3) {
        for (face in BlockFace.entries) {
            val neighborBlock = getBlock(pos.getSide(face))
            if (neighborBlock.id === BlockID.OBSERVER) neighborBlock.onNeighborChange(face.getOpposite())
        }
    }

    fun updateAround(x: Int, y: Int, z: Int) {
        updateAround(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
    }

    fun scheduleUpdate(pos: Block, delay: Int) {
        this.scheduleUpdate(pos, pos.position, delay, 0, true)
    }

    fun scheduleUpdate(pos: Block, delay: Int, checkBlockWhenUpdate: Boolean) {
        this.scheduleUpdate(pos, pos.position, delay, 0, true, checkBlockWhenUpdate)
    }

    @JvmOverloads
    fun scheduleUpdate(
        block: Block,
        pos: Vector3,
        delay: Int,
        priority: Int = 0,
        checkArea: Boolean = true,
        checkBlockWhenUpdate: Boolean = true
    ) {
        if (block.id == BlockID.AIR || (checkArea && !this.isChunkLoaded(
                block.position.floorX shr 4,
                block.position.floorZ shr 4
            ))
        ) {
            return
        }

        val entry: BlockUpdateEntry =
            BlockUpdateEntry(pos.floor(), block, delay + currentTick, priority, checkBlockWhenUpdate)

        if (!updateQueue.contains(entry)) {
            updateQueue.add(entry)
        }
    }

    fun cancelSheduledUpdate(pos: Vector3, block: Block): Boolean {
        return updateQueue.remove(BlockUpdateEntry(pos, block))
    }

    fun isUpdateScheduled(pos: Vector3, block: Block): Boolean {
        return updateQueue.contains(BlockUpdateEntry(pos, block))
    }

    fun isBlockTickPending(pos: Vector3, block: Block): Boolean {
        return updateQueue.isBlockTickPending(pos, block)
    }

    fun getPendingBlockUpdates(chunk: IChunk): Set<BlockUpdateEntry> {
        val minX = (chunk.x shl 4) - 2
        val maxX = minX + 16 + 2
        val minZ = (chunk.z shl 4) - 2
        val maxZ = minZ + 16 + 2

        return this.getPendingBlockUpdates(
            SimpleAxisAlignedBB(
                minX.toDouble(),
                (if (isOverWorld) -64 else 0).toDouble(),
                minZ.toDouble(),
                maxX.toDouble(),
                (if (isOverWorld) 320 else 256).toDouble(),
                maxZ.toDouble()
            )
        )
    }

    fun getPendingBlockUpdates(boundingBox: AxisAlignedBB): Set<BlockUpdateEntry> {
        return updateQueue.getPendingBlockUpdates(boundingBox)
    }

    fun scanBlocks(bb: AxisAlignedBB, condition: BiPredicate<BlockVector3?, BlockState?>): List<Block> {
        val min = Vector3(
            floor(bb.minX),
            floor(bb.minY),
            floor(bb.minZ)
        ).asBlockVector3()
        val max = Vector3(
            floor(bb.maxX),
            floor(bb.maxY),
            floor(bb.maxZ)
        ).asBlockVector3()
        val minChunk: ChunkVector2 = min.chunkVector
        val maxChunk: ChunkVector2 = max.chunkVector
        return IntStream.rangeClosed(minChunk.x, maxChunk.x)
            .mapToObj { x: Int ->
                IntStream.rangeClosed(minChunk.z, maxChunk.z).mapToObj { z: Int -> ChunkVector2(x, z) }
            }
            .flatMap(Function.identity<Stream<ChunkVector2>>())
            .parallel()
            .map<IChunk> { pos: ChunkVector2 -> this.getChunk(pos) }
            .filter { obj: IChunk? -> Objects.nonNull(obj) }
            .flatMap<Block> { chunk: IChunk ->
                chunk.scanBlocks(
                    min,
                    max,
                    condition
                )
            }
            .toList()
    }

    fun raycastBlocks(start: Vector3, end: Vector3): List<Block> {
        return raycastBlocks(start, end, true, false, 1.0)
    }

    fun raycastBlocks(start: Vector3, end: Vector3, ignoreAir: Boolean, load: Boolean, space: Double): List<Block> {
        val result = mutableListOf<Block>()
        val direction = end.subtract(start).normalize()
        var currentPos = start.clone()

        var i = 0.0
        while (i < start.distance(end)) {
            val block = this.getBlock(currentPos.floor(), load)
            currentPos = currentPos.add(direction)
            if (!block.isAir || !ignoreAir) result.add(block)
            i += space
        }
        return result
    }

    fun getCollisionBlocks(bb: AxisAlignedBB): Array<Block> {
        return this.getCollisionBlocks(bb, false)
    }

    fun getCollisionBlocks(bb: AxisAlignedBB, targetFirst: Boolean): Array<Block> {
        return getCollisionBlocks(bb, targetFirst, false)
    }

    fun getCollisionBlocks(bb: AxisAlignedBB, targetFirst: Boolean, ignoreCollidesCheck: Boolean): Array<Block> {
        return getCollisionBlocks(
            bb, targetFirst, ignoreCollidesCheck
        ) { block: Block -> block.id != BlockID.AIR }
    }

    fun getCollisionBlocks(
        bb: AxisAlignedBB,
        targetFirst: Boolean,
        ignoreCollidesCheck: Boolean,
        condition: Predicate<Block>
    ): Array<Block> {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()

        val collides: MutableList<Block> = ArrayList()

        if (targetFirst) {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block =
                            this.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()), false)
                        if (block != null && condition.test(block) && (ignoreCollidesCheck || block.collidesWithBB(bb))) {
                            return arrayOf(block)
                        }
                    }
                }
            }
        } else {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block =
                            this.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()), false)
                        if (block != null && condition.test(block) && (ignoreCollidesCheck || block.collidesWithBB(bb))) {
                            collides.add(block)
                        }
                    }
                }
            }
        }

        return collides.toTypedArray()
    }

    fun isFullBlock(pos: IVector3): Boolean {
        val bb: AxisAlignedBB?
        if (pos is Block) {
            if (pos.isSolid) {
                return true
            }
            bb = pos.boundingBox
        } else {
            bb = getBlock(pos.vector3).boundingBox
        }

        return bb != null && bb.averageEdgeLength >= 1
    }

    fun getTickCachedCollisionBlocks(bb: AxisAlignedBB): Array<Block> {
        return this.getTickCachedCollisionBlocks(bb, false)
    }

    fun getTickCachedCollisionBlocks(bb: AxisAlignedBB, targetFirst: Boolean): Array<Block> {
        return getTickCachedCollisionBlocks(bb, targetFirst, false)
    }

    fun getTickCachedCollisionBlocks(
        bb: AxisAlignedBB,
        targetFirst: Boolean,
        ignoreCollidesCheck: Boolean
    ): Array<Block> {
        return getTickCachedCollisionBlocks(
            bb, targetFirst, ignoreCollidesCheck
        ) { block: Block -> block.id != BlockID.AIR }
    }

    fun getTickCachedCollisionBlocks(
        bb: AxisAlignedBB,
        targetFirst: Boolean,
        ignoreCollidesCheck: Boolean,
        condition: Predicate<Block>
    ): Array<Block> {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()

        val collides: MutableList<Block> = ArrayList()

        if (targetFirst) {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block = this.getTickCachedBlock(
                            temporalVector.setComponents(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble()
                            ), false
                        )
                        if (block != null && condition.test(block) && (ignoreCollidesCheck || block.collidesWithBB(bb))) {
                            return arrayOf(block)
                        }
                    }
                }
            }
        } else {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block = this.getTickCachedBlock(
                            temporalVector.setComponents(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble()
                            ), false
                        )
                        if (block != null && condition.test(block) && (ignoreCollidesCheck || block.collidesWithBB(bb))) {
                            collides.add(block)
                        }
                    }
                }
            }
        }

        return collides.toTypedArray()
    }

    fun getCollisionCubes(entity: Entity?, bb: AxisAlignedBB): Array<AxisAlignedBB> {
        return this.getCollisionCubes(entity, bb, true)
    }

    fun getCollisionCubes(entity: Entity?, bb: AxisAlignedBB, entities: Boolean): Array<AxisAlignedBB> {
        return getCollisionCubes(entity, bb, entities, false)
    }

    fun getCollisionCubes(
        entity: Entity?,
        bb: AxisAlignedBB,
        entities: Boolean,
        solidEntities: Boolean
    ): Array<AxisAlignedBB> {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()

        val collides: MutableList<AxisAlignedBB> = ArrayList<AxisAlignedBB>()

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val block =
                        this.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()), false)
                    if (block.boundingBox != null && !block.canPassThrough() && block.collidesWithBB(bb)) {
                        collides.add(block.boundingBox!!)
                    }
                }
            }
        }

        if (entities || solidEntities) {
            for (ent in this.getCollidingEntities(bb.grow(0.25, 0.25, 0.25), entity)) {
                if (solidEntities && !ent.canPassThrough()) {
                    collides.add(ent.boundingBox.clone())
                }
            }
        }

        return collides.toTypedArray()
    }

    @JvmOverloads
    fun fastCollisionCubes(entity: Entity?, bb: AxisAlignedBB, entities: Boolean = true): List<AxisAlignedBB> {
        return fastCollisionCubes(entity, bb, entities, false)
    }

    fun fastCollisionCubes(
        entity: Entity?,
        bb: AxisAlignedBB,
        entities: Boolean,
        solidEntities: Boolean
    ): List<AxisAlignedBB> {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()

        val collides: MutableList<AxisAlignedBB> = ArrayList<AxisAlignedBB>()

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val block =
                        this.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()), false)
                    if (block.boundingBox != null && !block.canPassThrough() && block.collidesWithBB(bb)) {
                        collides.add(block.boundingBox!!)
                    }
                }
            }
        }

        if (entities || solidEntities) {
            val grownBB: AxisAlignedBB = bb.grow(0.25, 0.25, 0.25)
            collides.addAll(
                streamCollidingEntities(grownBB, entity)
                    .filter { ent: Entity? -> solidEntities && !ent!!.canPassThrough() }.map<AxisAlignedBB>(
                        Function<Entity?, AxisAlignedBB> { ent: Entity? -> ent!!.boundingBox.clone() }).toList()
            )
        }

        return collides
    }

    fun hasCollision(entity: Entity?, bb: AxisAlignedBB, entities: Boolean): Boolean {
        val minX = floor(round(bb.minX, 4)).toInt()
        val minY = floor(round(bb.minY, 4)).toInt()
        val minZ = floor(round(bb.minZ, 4)).toInt()
        val maxX = ceil(round(bb.maxX, 4) - 0.00001).toInt()
        val maxY = ceil(round(bb.maxY, 4) - 0.00001).toInt()
        val maxZ = ceil(round(bb.maxZ, 4) - 0.00001).toInt()

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val block = this.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
                    if (!block.canPassThrough() && block.collidesWithBB(bb)) {
                        return true
                    }
                }
            }
        }

        if (entities) {
            return getCollidingEntities(bb.grow(0.25, 0.25, 0.25), entity).size > 0
        }
        return false
    }

    fun getFullLight(pos: Vector3): Int {
        val chunk = this.getChunk(pos.x.toInt() shr 4, pos.z.toInt() shr 4, false)
        var level = 0
        if (chunk != null) {
            level = chunk.getBlockSkyLight(pos.x.toInt() and 0x0f, ensureY(pos.y.toInt()), pos.z.toInt() and 0x0f)
            level -= this.skyLightSubtracted
            if (level < 15) {
                level = Math.max(
                    chunk.getBlockLight(
                        pos.x.toInt() and 0x0f, ensureY(pos.y.toInt()), pos.z.toInt() and 0x0f
                    ),
                    level
                )
            }
        }
        return level
    }

    fun calculateSkylightSubtracted(tickDiff: Float): Int {
        val d = 1.0f - (this.getRainStrength(tickDiff) * 5.0f) / 16.0f
        val e = 1.0f - (this.getThunderStrength(tickDiff) * 5.0f) / 16.0f
        val f =
            0.5f + 2.0f * cos(this.getCelestialAngle(tickDiff) * 6.2831855f).coerceIn(-0.25f, 0.25f)
        return ((1.0f - f * d * e) * 11.0f).toInt()
        /* Old NukkitX Code
        float angle = this.getCelestialAngle(tickDiff);
        float light = 1.0F - (cos(angle * ((float) Math.PI * 2F)) * 2.0F + 0.5F);
        light = light.coerceIn(0.0F, 1.0F);
        light = 1.0F - light;
        light = (float) ((double) light * (1.0D - (double) (this.getRainStrength(tickDiff) * 5.0F) / 16.0D));
        light = (float) ((double) light * (1.0D - (double) (this.getThunderStrength(tickDiff) * 5.0F) / 16.0D));
        light = 1.0F - light;
        return (int) (light * 11.0F);
         */
    }

    fun getRainStrength(tickDiff: Float): Float {
        return (if (isRaining) 1 else 0 // TODO: real implementation
                ).toFloat()
    }

    fun getThunderStrength(tickDiff: Float): Float {
        return (if (isThundering()) 1 else 0 // TODO: real implementation
                ).toFloat()
    }

    fun getCelestialAngle(tickDiff: Float): Float {
        return calculateCelestialAngle(getTime(), tickDiff)
    }

    fun calculateCelestialAngle(time: Int, tickDiff: Float): Float {
        val i = (time % 24000L).toInt()
        var angle = (i.toFloat() + tickDiff) / 24000.0f - 0.25f

        if (angle < 0.0f) {
            ++angle
        }

        if (angle > 1.0f) {
            --angle
        }

        val f1 = 1.0f - ((Math.cos(angle.toDouble() * Math.PI) + 1.0) / 2.0).toFloat()
        angle = angle + (f1 - angle) / 3.0f
        return angle
    }

    fun getMoonPhase(worldTime: Long): Int {
        return (worldTime / 24000 % 8 + 8).toInt() % 8
    }

    fun getBlockRuntimeId(x: Int, y: Int, z: Int): Int {
        return getBlockRuntimeId(x, y, z, 0)
    }

    fun getBlockRuntimeId(x: Int, y: Int, z: Int, layer: Int): Int {
        val tmp = this.getChunk(x shr 4, z shr 4, false)
        return tmp.getBlockState(x and 0x0f, ensureY(y), z and 0x0f, layer).blockStateHash()
    }

    fun getBlockAround(pos: Vector3): Set<Block> {
        val around: MutableSet<Block> = HashSet()
        val block = getBlock(pos)
        for (face in BlockFace.entries) {
            val side = block.getSideAtLayer(0, face)
            around.add(side)
        }
        return around
    }

    fun getTickCachedBlock(pos: Vector3): Block {
        return getTickCachedBlock(pos, 0)
    }

    fun getTickCachedBlock(pos: Vector3, layer: Int): Block {
        return this.getTickCachedBlock(pos.floorX, pos.floorY, pos.floorZ, layer)
    }

    fun getTickCachedBlock(pos: Vector3, load: Boolean): Block {
        return getTickCachedBlock(pos, 0, load)
    }

    fun getTickCachedBlock(pos: Vector3, layer: Int, load: Boolean): Block {
        return this.getTickCachedBlock(pos.floorX, pos.floorY, pos.floorZ, layer, load)
    }

    fun getTickCachedBlock(x: Int, y: Int, z: Int): Block {
        return getTickCachedBlock(x, y, z, 0)
    }

    fun getTickCachedBlock(x: Int, y: Int, z: Int, layer: Int): Block {
        return getTickCachedBlock(x, y, z, layer, true)
    }

    fun getTickCachedBlock(x: Int, y: Int, z: Int, load: Boolean): Block {
        return getTickCachedBlock(x, y, z, 0, load)
    }

    fun getTickCachedBlock(x: Int, y: Int, z: Int, layer: Int, load: Boolean): Block {
        return tickCachedBlocks.computeIfAbsent(
            chunkHash(x shr 4, z shr 4)
        ) {
            SimpleTickCachedBlockStore(
                this
            )
        }.computeFromCachedStore(x, y, z, layer, object : TickCachedBlockStore.CachedBlockComputer {
            override fun compute(): Block {
                return getBlock(x, y, z, layer, load)
            }
        })
    }

    fun getBlock(pos: Vector3): Block {
        return getBlock(pos, 0)
    }

    fun getBlock(pos: Vector3, layer: Int): Block {
        return this.getBlock(pos.floorX, pos.floorY, pos.floorZ, layer)
    }

    fun getBlock(pos: Vector3, load: Boolean): Block {
        return getBlock(pos, 0, load)
    }

    fun getBlock(pos: Vector3, layer: Int, load: Boolean): Block {
        return this.getBlock(pos.floorX, pos.floorY, pos.floorZ, layer, load)
    }

    fun getBlock(x: Int, y: Int, z: Int): Block {
        return getBlock(x, y, z, 0)
    }

    fun getBlock(x: Int, y: Int, z: Int, layer: Int): Block {
        return getBlock(x, y, z, layer, true)
    }

    fun getBlock(x: Int, y: Int, z: Int, load: Boolean): Block {
        return getBlock(x, y, z, 0, load)
    }

    fun getBlock(x: Int, y: Int, z: Int, layer: Int, load: Boolean): Block {
        var fullState = BlockAir.properties.defaultState
        if (isYInRange(y)) {
            val cx = x shr 4
            val cz = z shr 4
            val chunk = if (load) {
                getChunk(cx, cz)
            } else {
                getChunkIfLoaded(cx, cz)
            }
            if (chunk != null) {
                fullState = chunk.getBlockState(x and 0xF, y, z and 0xF, layer)
            }
        }
        return Registries.BLOCK[fullState, x, y, z, layer, this]!!
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int): String {
        return getBlockIdAt(x, y, z, 0)
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int, layer: Int): String {
        return getChunk(x shr 4, z shr 4, true).getBlockState(x and 0x0f, ensureY(y), z and 0x0f, layer).identifier
    }

    fun updateAllLight(pos: Vector3) {
        this.updateBlockSkyLight(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        this.addLightUpdate(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
    }

    fun updateBlockSkyLight(x: Int, y: Int, z: Int) {
        val chunk = getChunkIfLoaded(x shr 4, z shr 4) ?: return

        val oldHeightMap = chunk.getHeightMap(x and 0xf, z and 0xf)
        val sourceBlock = getBlock(x, y, z)

        val newHeightMap: Int
        if (y == oldHeightMap) { // Block changed directly in the heightmap. Check if a block was removed or changed to a different light-filter
            newHeightMap = chunk.recalculateHeightMapColumn(x and 0x0f, z and 0x0f)
        } else if (y > oldHeightMap) { // Block changed above the heightmap
            if (sourceBlock.lightFilter > 1 || sourceBlock.diffusesSkyLight()) {
                chunk.setHeightMap(x and 0xf, z and 0xf, y)
                newHeightMap = y
            } else { // Block changed which has no effect on direct skylight, for example placing or removing glass.
                return
            }
        } else { // Block changed below heightmap
            newHeightMap = oldHeightMap
        }

        if (newHeightMap > oldHeightMap) { // Heightmap increase, block placed, remove skylight
            for (i in y downTo oldHeightMap) {
                setBlockSkyLightAt(x, i, z, 0)
            }
        } else if (newHeightMap < oldHeightMap) { // Heightmap decrease, block changed or removed, add skylight
            for (i in y downTo newHeightMap) {
                setBlockSkyLightAt(x, i, z, 15)
            }
        } else { // No heightmap change, block changed "underground"
            setBlockSkyLightAt(
                x,
                y,
                z,
                Math.max(0, getHighestAdjacentBlockSkyLight(x, y, z) - sourceBlock.lightFilter)
            )
        }
    }

    /**
     * Returns the highest block skylight level available in the positions adjacent to the specified block coordinates.
     */
    fun getHighestAdjacentBlockSkyLight(x: Int, y: Int, z: Int): Int {
        val lightLevels = intArrayOf(
            getBlockSkyLightAt(x + 1, y, z),
            getBlockSkyLightAt(x - 1, y, z),
            getBlockSkyLightAt(x, y + 1, z),
            getBlockSkyLightAt(x, y - 1, z),
            getBlockSkyLightAt(x, y, z + 1),
            getBlockSkyLightAt(x, y, z - 1),
        )

        var maxValue = lightLevels[0]
        for (i in 1..<lightLevels.size) {
            if (lightLevels[i] > maxValue) {
                maxValue = lightLevels[i]
            }
        }

        return maxValue
    }

    fun updateBlockLight(map: MutableMap<Long, MutableMap<Int, Any>>) {
        var size: Int = map.size
        if (size == 0) {
            return
        }
        val lightPropagationQueue: Queue<Long> = ConcurrentLinkedQueue()
        val lightRemovalQueue: Queue<Array<Any>> = ConcurrentLinkedQueue()
        val visited = mutableMapOf<Long, Any>()
        val removalVisited = mutableMapOf<Long, Any>()

        val iter = map.entries.iterator()
        while (iter.hasNext() && size-- > 0) {
            val entry = iter.next()
            iter.remove()
            val index = entry.key
            val blocks = entry.value
            val chunkX = getHashX(index)
            val chunkZ = getHashZ(index)
            val bx = chunkX shl 4
            val bz = chunkZ shl 4
            for (blockHash in blocks.keys) {
                val hi = (blockHash ushr 16).toByte().toInt()
                val lo = blockHash.toShort().toInt()
                val y = ensureY(lo - 64)
                val x = (hi and 0xF) + bx
                val z = ((hi shr 4) and 0xF) + bz
                val chunk = getChunk(x shr 4, z shr 4, false)
                if (chunk != null) {
                    val lcx = x and 0xF
                    val lcz = z and 0xF
                    val oldLevel = chunk.getBlockLight(lcx, y, lcz)
                    val newLevel = Registries.BLOCK[chunk.getBlockState(lcx, y, lcz)!!, x, y, z, this]!!.lightLevel
                    if (oldLevel != newLevel) {
                        this.setBlockLightAt(x, y, z, newLevel)
                        if (newLevel < oldLevel) {
                            removalVisited.put(Hash.hashBlock(x, y, z), changeBlocksPresent)
                            lightRemovalQueue.add(arrayOf(Hash.hashBlock(x, y, z), oldLevel))
                        } else {
                            visited.put(Hash.hashBlock(x, y, z), changeBlocksPresent)
                            lightPropagationQueue.add(Hash.hashBlock(x, y, z))
                        }
                    }
                }
            }
        }

        while (!lightRemovalQueue.isEmpty()) {
            val `val` = lightRemovalQueue.poll()
            val node = `val`[0] as Long
            val x = Hash.hashBlockX(node)
            val y = Hash.hashBlockY(node)
            val z = Hash.hashBlockZ(node)

            val lightLevel = `val`[1] as Int

            this.computeRemoveBlockLight(
                x - 1, y, z, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
            this.computeRemoveBlockLight(
                x + 1, y, z, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
            this.computeRemoveBlockLight(
                x, y - 1, z, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
            this.computeRemoveBlockLight(
                x, y + 1, z, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
            this.computeRemoveBlockLight(
                x, y, z - 1, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
            this.computeRemoveBlockLight(
                x, y, z + 1, lightLevel, lightRemovalQueue, lightPropagationQueue,
                removalVisited, visited
            )
        }

        while (!lightPropagationQueue.isEmpty()) {
            val node = lightPropagationQueue.poll()

            val x = Hash.hashBlockX(node)
            val y = Hash.hashBlockY(node)
            val z = Hash.hashBlockZ(node)
            val lightLevel = this.getBlockLightAt(x, y, z) - getBlock(x, y, z).lightFilter

            if (lightLevel >= 1) {
                this.computeSpreadBlockLight(x - 1, y, z, lightLevel, lightPropagationQueue, visited)
                this.computeSpreadBlockLight(x + 1, y, z, lightLevel, lightPropagationQueue, visited)
                this.computeSpreadBlockLight(x, y - 1, z, lightLevel, lightPropagationQueue, visited)
                this.computeSpreadBlockLight(x, y + 1, z, lightLevel, lightPropagationQueue, visited)
                this.computeSpreadBlockLight(x, y, z - 1, lightLevel, lightPropagationQueue, visited)
                this.computeSpreadBlockLight(x, y, z + 1, lightLevel, lightPropagationQueue, visited)
            }
        }
    }

    private fun computeRemoveBlockLight(
        x: Int, y: Int, z: Int, currentLight: Int, queue: Queue<Array<Any>>,
        spreadQueue: Queue<Long>, visited: MutableMap<Long, Any>, spreadVisited: MutableMap<Long, Any>
    ) {
        val current = this.getBlockLightAt(x, y, z)
        val index = Hash.hashBlock(x, y, z)
        if (current != 0 && current < currentLight) {
            this.setBlockLightAt(x, y, z, 0)
            if (current > 1) {
                if (!visited.containsKey(index)) {
                    visited[index] = changeBlocksPresent
                    queue.add(arrayOf(Hash.hashBlock(x, y, z), current))
                }
            }
        } else if (current >= currentLight) {
            if (!spreadVisited.containsKey(index)) {
                spreadVisited[index] = changeBlocksPresent
                spreadQueue.add(Hash.hashBlock(x, y, z))
            }
        }
    }

    private fun computeSpreadBlockLight(
        x: Int, y: Int, z: Int, currentLight: Int, queue: Queue<Long>,
        visited: MutableMap<Long, Any>
    ) {
        val current = this.getBlockLightAt(x, y, z)
        val index = Hash.hashBlock(x, y, z)

        if (current < currentLight - 1) {
            this.setBlockLightAt(x, y, z, currentLight)

            if (!visited.containsKey(index)) {
                visited[index] = changeBlocksPresent
                if (currentLight > 1) {
                    queue.add(Hash.hashBlock(x, y, z))
                }
            }
        }
    }

    fun addLightUpdate(x: Int, y: Int, z: Int) {
        val index = chunkHash(x shr 4, z shr 4)
        var currentMap = lightQueue[index]
        if (currentMap == null) {
            currentMap = ConcurrentHashMap(8, 0.9f, 1)
            lightQueue.put(index, currentMap)
        }
        currentMap[localBlockHash(x.toDouble(), y.toDouble(), z.toDouble(), this)] =
            changeBlocksPresent
    }

    fun setBlock(pos: Vector3, block: Block): Boolean {
        return setBlock(pos, 0, block)
    }

    fun setBlock(pos: Vector3, layer: Int, block: Block): Boolean {
        return this.setBlock(pos, layer, block, false)
    }

    fun setBlock(pos: Vector3, block: Block, direct: Boolean): Boolean {
        return this.setBlock(pos, 0, block, direct)
    }

    fun setBlock(pos: Vector3, layer: Int, block: Block, direct: Boolean): Boolean {
        return this.setBlock(pos, layer, block, direct, true)
    }

    fun setBlock(pos: Vector3, block: Block, direct: Boolean, update: Boolean): Boolean {
        return setBlock(pos, 0, block, direct, update)
    }

    fun setBlock(pos: Vector3, layer: Int, block: Block, direct: Boolean, update: Boolean): Boolean {
        return setBlock(pos.floorX, pos.floorY, pos.floorZ, layer, block, direct, update)
    }

    fun setBlock(x: Int, y: Int, z: Int, block: Block, direct: Boolean, update: Boolean): Boolean {
        return setBlock(x, y, z, 0, block, direct, update)
    }

    /**
     * Sets a block at the specified position and determines whether to immediately synchronize changes to clients or perform block update tick.
     *
     * @param x      The x-coordinate of the block.
     * @param y      The y-coordinate of the block. Must be within the valid range.
     * @param z      The z-coordinate of the block.
     * @param layer  The block layer to set, used for handling layered blocks like water (e.g., blocks beneath water).
     * @param block  The block to be set.
     * @param direct Whether to immediately synchronize changes to clients
     * @param update Whether to perform update on block, such as lighting, event, cause around update etc.
     * @return True if the block was successfully set, otherwise false.
     */
    fun setBlock(x: Int, y: Int, z: Int, layer: Int, block: Block, direct: Boolean, update: Boolean): Boolean {
        var block = block
        if (!isYInRange(y) || layer < 0 || layer > requireProvider().maximumLayer) {
            return false
        }

        val state = block.blockState
        val chunk = this.getChunk(x shr 4, z shr 4, true)
        val statePrevious = chunk.getAndSetBlockState(x and 0xF, y, z and 0xF, state, layer)

        if (state === statePrevious) {
            return false
        }

        block.position.x = x.toDouble()
        block.position.y = y.toDouble()
        block.position.z = z.toDouble()
        block.level = this
        block.layer = layer

        val blockPrevious = statePrevious!!.toBlock()
        blockPrevious.position.x = x.toDouble()
        blockPrevious.position.y = y.toDouble()
        blockPrevious.position.z = z.toDouble()
        blockPrevious.level = this
        blockPrevious.layer = layer

        val cx = x shr 4
        val cz = z shr 4
        val index = chunkHash(cx, cz)

        if (direct) {
            if (isAntiXrayEnabled && block.isTransparent) {
                this.sendBlocks(
                    getChunkPlayers(cx, cz).values.toTypedArray(),
                    arrayOf<Vector3?>(
                        block.position.add(-1.0),
                        block.position.add(1.0),
                        block.position.add(0.0, -1.0),
                        block.position.add(0.0, 1.0),
                        block.position.add(0.0, 0.0, 1.0),
                        block.position.add(0.0, 0.0, -1.0)
                    ),
                    org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY
                )
            }
            this.sendBlocks(
                getChunkPlayers(cx, cz).values.toTypedArray(),
                arrayOf<Block?>(block),
                org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY,
                block.layer
            )
        } else {
            addBlockChange(index, x, y, z)
        }

        if (update) {
            if (Server.instance.settings.chunkSettings.lightUpdates) {
                updateAllLight(block.position)
            }

            val ev: BlockUpdateEvent = BlockUpdateEvent(block)
            Server.instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                for (entity in this.getNearbyEntities(
                    SimpleAxisAlignedBB(
                        (x - 1).toDouble(),
                        (y - 1).toDouble(),
                        (z - 1).toDouble(),
                        (x + 1).toDouble(),
                        (y + 1).toDouble(),
                        (z + 1).toDouble()
                    )
                )) {
                    entity.scheduleUpdate()
                }

                block = ev.block
                block.onUpdate(BLOCK_UPDATE_NORMAL)
                block.getLevelBlockAtLayer(if (layer == 0) 1 else 0).onUpdate(BLOCK_UPDATE_NORMAL)
                this.updateAround(x, y, z)

                if (block.hasComparatorInputOverride()) {
                    this.updateComparatorOutputLevel(block.position)
                }
            }
        }

        blockPrevious.afterRemoval(block, update)
        return true
    }

    fun breakBlock(block: Block) {
        if (block.level === this) {
            this.setBlock(block.position, Block.get(BlockID.AIR))
            val locator = block.add(0.5, 0.5, 0.5)
            this.addParticle(DestroyBlockParticle(locator.position, block))
            vibrationManager.callVibrationEvent(VibrationEvent(null, locator.position, VibrationType.BLOCK_DESTROY))
        }
    }

    private fun addBlockChange(x: Int, y: Int, z: Int) {
        val index = chunkHash(x shr 4, z shr 4)
        addBlockChange(index, x, y, z)
    }

    private fun addBlockChange(index: Long, x: Int, y: Int, z: Int) {
        synchronized(changedBlocks) {
            val current = changedBlocks.computeIfAbsent(
                index,
                { SoftReference(HashMap()) }
            )
            val currentMap = current.get()
            if (currentMap !== changeBlocksFullMap && currentMap != null) {
                if (currentMap.size > MAX_BLOCK_CACHE) {
                    changedBlocks[index] = SoftReference(changeBlocksFullMap)
                } else {
                    currentMap[localBlockHash(
                        x.toDouble(), y.toDouble(), z.toDouble(),
                        this
                    )] = changeBlocksPresent
                }
            }
        }
    }

    @JvmOverloads
    fun dropItem(source: Vector3, item: Item, motion: Vector3? = null, delay: Int = 10) {
        this.dropItem(source, item, motion, false, delay)
    }

    fun dropItem(source: Vector3, item: Item, motion: Vector3?, dropAround: Boolean, delay: Int) {
        var motion = motion
        if (motion == null) {
            if (dropAround) {
                val f = ThreadLocalRandom.current().nextFloat() * 0.5f
                val f1 = ThreadLocalRandom.current().nextFloat() * (Math.PI.toFloat() * 2)

                motion = Vector3(
                    (-sin(f1) * f).toDouble(),
                    0.20000000298023224,
                    (cos(f1) * f).toDouble()
                )
            } else {
                motion = Vector3(
                    Random().nextDouble() * 0.2 - 0.1, 0.2,
                    Random().nextDouble() * 0.2 - 0.1
                )
            }
        }

        if (item.isNothing) {
            return
        }
        val itemEntity = createEntity(
            EntityID.ITEM,
            this.getChunk(source.x.toInt() shr 4, source.z.toInt() shr 4, true),
            getDefaultNBT(source, motion, Random().nextFloat() * 360, 0f)
                .putShort("Health", 5)
                .putCompound("Item", NBTIO.putItemHelper(item))
                .putShort("PickupDelay", delay)
        ) as EntityItem?

        if (itemEntity != null) {
            itemEntity.spawnToAll()
        }
    }

    @JvmOverloads
    fun dropAndGetItem(source: Vector3, item: Item, motion: Vector3? = null, delay: Int = 10): EntityItem? {
        return this.dropAndGetItem(source, item, motion, false, delay)
    }

    fun dropAndGetItem(source: Vector3, item: Item, motion: Vector3?, dropAround: Boolean, delay: Int): EntityItem? {
        var motion1 = motion
        if (item.isNothing) {
            return null
        }
        if (motion1 == null) {
            if (dropAround) {
                val f = ThreadLocalRandom.current().nextFloat() * 0.5f
                val f1 = ThreadLocalRandom.current().nextFloat() * (Math.PI.toFloat() * 2)

                motion1 = Vector3(
                    (-sin(f1) * f).toDouble(),
                    0.20000000298023224,
                    (cos(f1) * f).toDouble()
                )
            } else {
                motion1 = Vector3(
                    Random().nextDouble() * 0.2 - 0.1, 0.2,
                    Random().nextDouble() * 0.2 - 0.1
                )
            }
        }

        val itemTag = NBTIO.putItemHelper(item)

        val itemEntity: EntityItem? = createEntity(
            EntityID.ITEM,
            this.getChunk(source.x.toInt() shr 4, source.z.toInt() shr 4, true),
            CompoundTag().putList(
                "Pos", ListTag<FloatTag>().add(FloatTag(source.x))
                    .add(FloatTag(source.y)).add(FloatTag(source.z))
            )

                .putList(
                    "Motion", ListTag<FloatTag>().add(FloatTag(motion1.x))
                        .add(FloatTag(motion1.y)).add(FloatTag(motion1.z))
                )

                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(ThreadLocalRandom.current().nextFloat() * 360))
                        .add(FloatTag(0f))
                )

                .putShort("Health", 5).putCompound("Item", itemTag).putShort("PickupDelay", delay)
        ) as EntityItem?

        itemEntity?.spawnToAll()

        return itemEntity
    }

    @JvmOverloads
    fun useBreakOn(
        vector: IVector3,
        item: Item? = null,
        player: Player? = null,
        createParticles: Boolean = false
    ): Item? {
        return useBreakOn(vector, null, item, player, createParticles)
    }

    @JvmOverloads
    fun useBreakOn(
        vector: IVector3,
        face: BlockFace?,
        item: Item?,
        player: Player?,
        createParticles: Boolean,
        immediateDestroy: Boolean = false
    ): Item? {
        return if (vector is Block) {
            useBreakOn(vector.position, vector.layer, face, item, player, createParticles, immediateDestroy)
        } else {
            useBreakOn(vector.vector3, 0, face, item, player, createParticles, immediateDestroy)
        }
    }

    fun useBreakOn(
        vector: IVector3,
        layer: Int,
        face: BlockFace?,
        item: Item?,
        player: Player?,
        createParticles: Boolean,
        immediateDestroy: Boolean
    ): Item? {
        var item = item
        if (player != null && player.gamemode > 2) {
            return null
        }

        val target = this.getBlock(vector.vector3, layer)

        if (player != null && !target.isBlockChangeAllowed(player)) {
            return null
        }

        val drops: Array<Item>?
        var dropExp = target.dropExp

        if (item == null) {
            item = Item.AIR
        }

        if (!target.isBreakable(vector.vector3, layer, face, item, player)) {
            return null
        }

        val isSilkTouch = target.isSilkTouch(vector.vector3, layer, face, item, player) ||
                (item.getEnchantment(Enchantment.ID_SILK_TOUCH) != null && item.applyEnchantments())

        if (player != null) {
            val eventDrops = if (immediateDestroy || player.isCreative) {
                Item.EMPTY_ARRAY
            } else if (isSilkTouch && target.canSilkTouch()) {
                arrayOf(target.toItem())
            } else {
                target.getDrops(item)
            }

            if (immediateDestroy) {
                drops = eventDrops
            } else {
                if (!player.adventureSettings.get(org.chorus_oss.protocol.types.PlayerAbility.Mine)) return null

                //使用calculateBreakTimeNotInAir目的是获取玩家在陆地上的挖掘时间，如果挖掘时间小于这个时间才认为玩家作弊。
                var breakTime = target.calculateBreakTimeNotInAir(item, player)
                //对于自定义方块，由于用户可以自由设置客户端侧的挖掘时间，拿服务端硬度计算出来的挖掘时间来判断是否为fastBreak是不准确的。
                if (target is CustomBlock) {
                    val comp: CompoundTag = target.definition.nbt.getCompound("components")
                    if (comp.containsCompound("minecraft:destructible_by_mining")) {
                        val clientBreakTime = comp.getCompound("minecraft:destructible_by_mining").getFloat("value")
                        breakTime = Math.min(breakTime, clientBreakTime.toDouble())
                    }
                }
                if (player.isCreative && breakTime > 0.15) {
                    breakTime = 0.15
                }
                breakTime -= 0.15
                //thisBreak-lastBreak < breakTime-1000ms = the player is hacker (fastBreak)
                val fastBreak = java.lang.Long.sum(player.lastBreak, breakTime.toLong() * 1000) > java.lang.Long.sum(
                    System.currentTimeMillis(), 1000
                )
                val ev: BlockBreakEvent =
                    BlockBreakEvent(player, target, face, item, eventDrops, player.isCreative, fastBreak)
                if (!player.isOp && isInSpawnRadius(target.position)) {
                    ev.cancelled = true
                } else if (!ev.instaBreak && ev.isFastBreak) {
                    ev.cancelled = true
                }

                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return null
                }

                if (!ev.instaBreak && ev.isFastBreak) {
                    return null
                }

                player.lastBreak = System.currentTimeMillis()

                drops = ev.drops
                dropExp = ev.dropExp
            }
        } else if (isSilkTouch) {
            drops = arrayOf(target.toItem())
        } else {
            drops = target.getDrops(item)
        }

        val above =
            this.getBlock(Vector3(target.position.x, target.position.y + 1, target.position.z), 0)
        if (above != null) {
            if (above.id == BlockID.FIRE) {
                this.setBlock(above.position, Block.get(BlockID.AIR), true)
            }
        }

        if (createParticles) {
            val players: MutableMap<Int, Player> = this.getChunkPlayers(
                target.position.x.toInt() shr 4, target.position.z.toInt() shr 4
            )
            if (player != null && immediateDestroy) {
                players.remove(player.loaderId)
            }
            this.addParticle(DestroyBlockParticle(target.position.add(0.5), target), players.values)
        }

        // Close BlockEntity before we check onBreak
        if (layer == 0) {
            val blockEntity = this.getBlockEntity(target.position)
            if (blockEntity != null) {
                blockEntity.onBreak(isSilkTouch)
                blockEntity.close()

                this.updateComparatorOutputLevel(target.position)
            }
        }

        target.onBreak(item)

        vibrationManager.callVibrationEvent(
            VibrationEvent(
                player,
                target.position.add(0.5, 0.5, 0.5),
                VibrationType.BLOCK_DESTROY
            )
        )

        item.useOn(target)
        if (item.isTool && item.damage >= item.maxDurability) {
            if (player != null) {
                addSound(player.position, Sound.RANDOM_BREAK)
            }
            item = Item.AIR
        }

        if (gameRules.getBoolean(GameRule.DO_TILE_DROPS)) {
            if (!isSilkTouch && (player != null && ((player.isSurvival || player.isAdventure || immediateDestroy))) && dropExp > 0) {
                this.dropExpOrb(vector.vector3.add(0.5, 0.5, 0.5), dropExp)
            }

            for (drop in drops) {
                if (drop.getCount() > 0) {
                    this.dropItem(vector.vector3.add(0.5, 0.5, 0.5), drop)
                }
            }
        }
        return item
    }

    @JvmOverloads
    fun dropExpOrb(source: Vector3, exp: Int, motion: Vector3? = null, delay: Int = 10) {
        dropExpOrbAndGetEntities(source, exp, motion, delay)
    }

    @JvmOverloads
    fun dropExpOrbAndGetEntities(source: Vector3, exp: Int, motion: Vector3? = null): List<EntityXpOrb> {
        return dropExpOrbAndGetEntities(source, exp, motion, 10)
    }

    fun dropExpOrbAndGetEntities(source: Vector3, exp: Int, motion: Vector3?, delay: Int): List<EntityXpOrb> {
        val rand: Random = ThreadLocalRandom.current()
        val drops: List<Int> = EntityXpOrb.splitIntoOrbSizes(exp)
        val entities: MutableList<EntityXpOrb> = ArrayList<EntityXpOrb>(drops.size)
        for (split in drops) {
            val nbt = getDefaultNBT(
                source, motion
                    ?: Vector3(
                        (rand.nextDouble() * 0.2 - 0.1) * 2,
                        rand.nextDouble() * 0.4,
                        (rand.nextDouble() * 0.2 - 0.1) * 2
                    ),
                rand.nextFloat() * 360f, 0f
            )

            nbt.putShort("Value", split)
            nbt.putShort("PickupDelay", delay)

            val entity: EntityXpOrb? = createEntity(
                EntityID.XP_ORB,
                this.getChunk(source.chunkX, source.chunkZ), nbt
            ) as EntityXpOrb?
            if (entity != null) {
                entities.add(entity)
                entity.spawnToAll()
            }
        }
        return entities
    }

    @JvmOverloads
    fun useItemOn(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player? = null
    ): Item? {
        return this.useItemOn(vector, item, face, fx, fy, fz, player, true)
    }

    fun useItemOn(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        playSound: Boolean
    ): Item? {
        var item = item
        val target = this.getBlock(vector)
        var block = target.getSide(face)

        if (item.getSafeBlockState().identifier == BlockScaffolding.properties.identifier && face == BlockFace.UP && block.id == BlockID.SCAFFOLDING) {
            while (block is BlockScaffolding) {
                block = block.up()
            }
        }
        //handle height limit
        if (!isYInRange(block.position.y.toInt())) {
            return null
        }
        //handle height limit in nether
        if (block.position.y > 127 && this.dimension == DIMENSION_NETHER) {
            return null
        }

        if (target.isAir) {
            return null
        }
        if (player != null) {
            val ev: PlayerInteractEvent = PlayerInteractEvent(
                player,
                item,
                target,
                face,
                if (target.isAir) PlayerInteractEvent.Action.RIGHT_CLICK_AIR else PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            )
            //                                handle spawn protect
            if (player.gamemode > 2 || (!player.isOp && isInSpawnRadius(target.position))) {
                ev.cancelled = true
            }

            Server.instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                target.onTouch(vector, item, face, fx, fy, fz, player, ev.action)
                if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && target.canBeActivated() && target.onActivate(
                        item,
                        player,
                        face,
                        fx,
                        fy,
                        fz
                    )
                ) {
                    if (item.isTool && item.damage >= item.maxDurability) {
                        addSound(player.position, Sound.RANDOM_BREAK)
                        item = Item.AIR
                    }
                    return item
                }

                if (item.canBeActivated() && item.onActivate(
                        this, player, block,
                        target, face, fx.toDouble(), fy.toDouble(), fz.toDouble()
                    )
                ) {
                    if (item.getCount() <= 0) {
                        item = Item.AIR
                        return item
                    }
                }
            } else {
                if ((item is ItemBucket) && item.isWater) {
                    player.level!!.sendBlocks(
                        arrayOf<Player>(player),
                        arrayOf<Block?>(Block.get(BlockID.AIR, target)),
                        org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY,
                        1
                    )
                }
                return null
            }
        } else if (!target.isAir && target.canBeActivated() && target.onActivate(item, null, face, fx, fy, fz)) {
            if (item.isTool && item.damage >= item.maxDurability) {
                item = Item.AIR
            }
            return item
        }

        return placeBlock(item, face, fx, fy, fz, player, playSound, block, target)
    }

    private fun beforePlaceBlock(
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        playSound: Boolean,
        block: Block,
        target: Block
    ): Block? {
        var block = block
        val hand: Block
        if (item.canBePlaced()) {
            hand = item.getSafeBlockState().toBlock(block)
            hand.position(block)
        } else {
            return null
        }
        if (!(block.canBeReplaced() ||
                    (hand is BlockSlab && hand.id == block.id) ||
                    hand is BlockCandle //Special for candles
                    )
        ) {
            return null
        }

        //处理放置梯子,我们应该提前给hand设置方向,这样后面计算是否碰撞实体才准确
        if (hand is BlockLadder) {
            if (target is BlockLadder) {
                hand.setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.getOpposite().index)
            } else hand.setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

        //cause bug (eg: frog_spawn) (and I don't know what this is for)
        if (hand !is BlockFrogSpawn && target.canBeReplaced()) {
            block = target
            hand.position(block)
        }
        return hand
    }

    private fun placeBlock(
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        playSound: Boolean,
        block: Block,
        target: Block
    ): Item? {
        var item1 = item
        val hand = beforePlaceBlock(item1, face, fx, fy, fz, player, playSound, block, target) ?: return null
        if (!hand.canPassThrough() && hand.boundingBox != null) {
            var realCount = 0
            val entities = this.getCollidingEntities(hand.boundingBox!!)
            for (e in entities) {
                if (e is EntityProjectile || e is EntityItem || e is EntityXpOrb || e is EntityAreaEffectCloud ||
                    e is EntityFireworksRocket || e is EntityPainting || e === player ||
                    (e is Player && e.isSpectator)
                ) {
                    continue
                }
                ++realCount
            }
            if (player != null) {
                val diff: Vector3 = player.nextPosition.position.subtract(player.locator.position)
                val aabb: AxisAlignedBB = player.getBoundingBox().getOffsetBoundingBox(diff.x, diff.y, diff.z)
                if (aabb.intersectsWith(hand.boundingBox!!.shrink(0.02, 0.02, 0.02))) {
                    ++realCount
                }
            }
            if (realCount > 0) {
                // Entity in block
                return null
            }
        }

        if (player != null) {
            val canChangeBlock = block.isBlockChangeAllowed(player)
            if ((!player.adventureSettings.get(org.chorus_oss.protocol.types.PlayerAbility.Build) && !canChangeBlock) || !canChangeBlock) return null

            val event: BlockPlaceEvent = BlockPlaceEvent(player, hand, block, target, item1)
            if (player.isAdventure) {
                val tag = item1.getNamedTagEntry("CanPlaceOn")
                var canPlace = canChangeBlock
                if (tag is ListTag<*>) {
                    for (v in (tag as ListTag<Tag<*>>).all) {
                        if (v !is StringTag) {
                            continue
                        }
                        val entry = get(v.data)
                        if (!entry.isNothing && entry.getSafeBlockState().identifier == target.id) {
                            canPlace = true
                            break
                        }
                    }
                }
                if (!canPlace) {
                    event.cancelled = true
                }
            }
            if (!player.isOp && isInSpawnRadius(target.position)) {
                event.cancelled = true
            }

            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                return null
            }
        }

        if (hand.waterloggingLevel == 0 && hand.canBeFlowedInto() && (block is BlockLiquid || block.getLevelBlockAtLayer(
                1
            ) is BlockLiquid)
        ) {
            return null
        }

        if ((block is BlockLiquid) && block.usesWaterLogging()) {
            this.setBlock(block.position, 1, block, false, false)
            this.setBlock(block.position, 0, Block.get(BlockID.AIR), false, false)
            this.scheduleUpdate(block, 1)
        }

        if (!hand.place(item1, block, target, face, fx.toDouble(), fy.toDouble(), fz.toDouble(), player)) {
            this.setBlock(block.position, 0, block, true, false)
            this.setBlock(block.position, 1, Block.get(BlockID.AIR), true, false)
            return null
        }

        if (player != null) {
            if (!player.isCreative) {
                item1.setCount(item1.getCount() - 1)
            }
        }

        if (playSound) {
            this.addLevelSoundEvent(
                hand.position,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Place,
                hand.runtimeId
            )
        }

        if (item1.getCount() <= 0) {
            item1 = Item.AIR
        }

        vibrationManager.callVibrationEvent(
            VibrationEvent(
                player,
                block.position.add(0.5, 0.5, 0.5),
                VibrationType.BLOCK_PLACE
            )
        )
        return item1
    }

    fun isInSpawnRadius(vector3: Vector3): Boolean {
        val distance = Server.instance.spawnRadius
        if (distance > -1) {
            val t = Vector2(vector3.x, vector3.z)
            val s = Vector2(
                spawnLocation.position.x,
                spawnLocation.position.z
            )
            return t.distance(s) <= distance
        }
        return false
    }

    fun getEntity(entityId: Long): Entity? {
        return if (entities.containsKey(entityId)) entities[entityId] else null
    }

    fun getEntities(): Array<Entity> {
        return entities.values.toTypedArray()
    }

    fun getCollidingEntities(bb: AxisAlignedBB): List<Entity> {
        return this.getCollidingEntities(bb, null)
    }

    fun getCollidingEntities(bb: AxisAlignedBB, entity: Entity?): List<Entity> {
        val result: MutableList<Entity> = mutableListOf()

        if (entity == null || entity.canCollide()) {
            val minX = floor((bb.minX - 2) / 16).toInt()
            val maxX = ceil((bb.maxX + 2) / 16).toInt()
            val minZ = floor((bb.minZ - 2) / 16).toInt()
            val maxZ = ceil((bb.maxZ + 2) / 16).toInt()

            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    for (each in getChunkEntities(x, z, false).values) {
                        if ((entity == null || (each !== entity && entity.canCollideWith(each)))
                            && each.boundingBox.intersectsWith(bb)
                        ) {
                            result.add(each)
                        }
                    }
                }
            }
        }

        return result
    }

    fun streamCollidingEntities(bb: AxisAlignedBB, entity: Entity?): Stream<Entity> {
        if (entity == null || entity.canCollide()) {
            val minX = floor((bb.minX - 2) / 16).toInt()
            val maxX = ceil((bb.maxX + 2) / 16).toInt()
            val minZ = floor((bb.minZ - 2) / 16).toInt()
            val maxZ = ceil((bb.maxZ + 2) / 16).toInt()

            val allEntities: MutableList<Entity> = mutableListOf()

            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    allEntities.addAll(getChunkEntities(x, z, false).values)
                }
            }

            return allEntities.stream().filter { each ->
                (entity == null || (each !== entity && entity.canCollideWith(each))) && each.boundingBox.intersectsWith(
                    bb
                )
            }
        } else {
            return Stream.empty()
        }
    }

    fun getNearbyEntities(bb: AxisAlignedBB): List<Entity> {
        return this.getNearbyEntities(bb, null)
    }

    fun getNearbyEntities(bb: AxisAlignedBB, entity: Entity?): List<Entity> {
        return getNearbyEntities(bb, entity, false)
    }

    fun getNearbyEntities(bb: AxisAlignedBB, entity: Entity?, loadChunks: Boolean): List<Entity> {
        val minX = floor((bb.minX - 2) * 0.0625).toInt()
        val maxX = ceil((bb.maxX + 2) * 0.0625).toInt()
        val minZ = floor((bb.minZ - 2) * 0.0625).toInt()
        val maxZ = ceil((bb.maxZ + 2) * 0.0625).toInt()

        val result: MutableList<Entity> = mutableListOf()
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (ent in getChunkEntities(x, z, loadChunks).values) {
                    if (ent !== entity && ent.boundingBox.intersectsWith(bb)) {
                        result.add(ent)
                    }
                }
            }
        }
        return result
    }

    fun getBlockEntities(): Map<Long, BlockEntity> {
        return blockEntities
    }

    fun getBlockEntityById(blockEntityId: Long): BlockEntity? {
        return if (blockEntities.containsKey(blockEntityId)) blockEntities[blockEntityId] else null
    }

    fun getPlayers(): Map<Long, Player> {
        return players
    }

    fun getLoaders(): Map<Int, ChunkLoader> {
        return loaders
    }

    fun getBlockEntity(pos: Vector3): BlockEntity? {
        return getBlockEntity(pos.asBlockVector3())
    }

    fun getBlockEntity(pos: BlockVector3): BlockEntity? {
        val chunk = this.getChunk(pos.x shr 4, pos.z shr 4, false)

        if (chunk != null) {
            return chunk.getBlockEntity(pos.x and 0x0f, ensureY(pos.y), pos.z and 0x0f)
        }

        return null
    }

    fun getBlockEntityIfLoaded(pos: Vector3): BlockEntity? {
        val chunk = this.getChunkIfLoaded(pos.x.toInt() shr 4, pos.z.toInt() shr 4)

        if (chunk != null) {
            return chunk.getBlockEntity(pos.x.toInt() and 0x0f, ensureY(pos.y.toInt()), pos.z.toInt() and 0x0f)
        }

        return null
    }

    fun getChunkEntities(x: Int, z: Int): Map<Long, Entity> {
        return getChunkEntities(x, z, true)
    }

    fun getChunkEntities(x: Int, z: Int, loadChunks: Boolean): Map<Long, Entity> {
        val chunk = if (loadChunks) this.getChunk(x, z) else this.getChunkIfLoaded(x, z)
        return chunk?.entities ?: mapOf()
    }

    fun getChunkBlockEntities(X: Int, Z: Int): Map<Long, BlockEntity> {
        val chunk: IChunk
        return if ((getChunk(X, Z).also { chunk = it }) != null) chunk.blockEntities else Collections.emptyMap()
    }

    fun setBlockStateAt(x: Int, y: Int, z: Int, state: BlockState) {
        setBlockStateAt(x, y, z, 0, state)
    }

    fun getBlockStateAt(x: Int, y: Int, z: Int, layer: Int): BlockState {
        return getChunk(x shr 4, z shr 4, true)
            .getBlockState(x and 0x0f, ensureY(y), z and 0x0f, layer)
    }

    fun getBlockStateAt(x: Int, y: Int, z: Int): BlockState {
        return getBlockStateAt(x, y, z, 0)
    }

    fun setBlockStateAt(x: Int, y: Int, z: Int, layer: Int, state: BlockState) {
        val chunk = this.getChunk(x shr 4, z shr 4, true)
        chunk.setBlockState(x and 0x0f, ensureY(y), z and 0x0f, state, layer)
        addBlockChange(x, y, z)
        temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble())
        if (Server.instance.settings.chunkSettings.lightUpdates) {
            updateAllLight(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
        }
    }

    /**
     * @param x the x
     * @param y the y
     * @param z the z
     * @return The block skylight at this location
     */
    fun getBlockSkyLightAt(x: Int, y: Int, z: Int): Int {
        return getChunk(x shr 4, z shr 4, true).getBlockSkyLight(x and 0x0f, ensureY(y), z and 0x0f)
    }

    fun setBlockSkyLightAt(x: Int, y: Int, z: Int, level: Int) {
        getChunk(x shr 4, z shr 4, true).setBlockSkyLight(x and 0x0f, ensureY(y), z and 0x0f, level and 0x0f)
    }

    /**
     * @param x the x
     * @param y the y
     * @param z the z
     * @return The block light at this location
     */
    fun getBlockLightAt(x: Int, y: Int, z: Int): Int {
        return getChunk(x shr 4, z shr 4, true).getBlockLight(x and 0x0f, ensureY(y), z and 0x0f)
    }

    fun setBlockLightAt(x: Int, y: Int, z: Int, level: Int) {
        getChunk(x shr 4, z shr 4, true).setBlockLight(x and 0x0f, ensureY(y), z and 0x0f, level and 0x0f)
    }

    fun getBiomeId(x: Int, y: Int, z: Int): Int {
        return getChunk(x shr 4, z shr 4, true).getBiomeId(x and 0x0f, y, z and 0x0f)
    }

    fun setBiomeId(x: Int, y: Int, z: Int, biomeId: Int) {
        getChunk(x shr 4, z shr 4, true).setBiomeId(x and 0x0f, y, z and 0x0f, biomeId)
    }

    fun getHeightMap(x: Int, z: Int): Int {
        return getChunk(x shr 4, z shr 4, true).getHeightMap(x and 0x0f, z and 0x0f)
    }

    fun setHeightMap(x: Int, z: Int, value: Int) {
        getChunk(x shr 4, z shr 4, true).setHeightMap(x and 0x0f, z and 0x0f, value and 0x0f)
    }

    val chunks: Map<Long, IChunk>
        get() = requireProvider().loadedChunks

    fun getChunkIfLoaded(chunkX: Int, chunkZ: Int): IChunk? {
        val index = chunkHash(chunkX, chunkZ)
        return requireProvider().getLoadedChunk(index)
    }

    /**
     * Set chunk to the level provider
     */
    fun setChunk(chunkX: Int, chunkZ: Int, chunk: IChunk) {
        val oldChunk = this.getChunk(chunkX, chunkZ, false)

        if (oldChunk !== chunk) {
            val index = chunkHash(chunkX, chunkZ)
            val oldEntities = if (oldChunk != null) oldChunk.entities else Collections.emptyMap()

            val oldBlockEntities = if (oldChunk != null) oldChunk.blockEntities else Collections.emptyMap()

            //move oldChunk to new Chunk
            if (!oldEntities!!.isEmpty()) {
                val iter = oldEntities.entries.iterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    val entity: Entity = entry.value
                    chunk.addEntity(entity)
                    iter.remove()
                    oldChunk.removeEntity(entity)
                    entity.chunk = chunk
                }
            }

            if (!oldBlockEntities!!.isEmpty()) {
                val iter = oldBlockEntities.entries.iterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    val blockEntity: BlockEntity = entry.value
                    chunk.addBlockEntity(blockEntity)
                    iter.remove()
                    oldChunk.removeBlockEntity(blockEntity)
                }
            }

            requireProvider().setChunk(chunkX, chunkZ, chunk)
            chunk.setChanged()
            if (!this.isChunkInUse(index)) {
                this.unloadChunkRequest(chunkX, chunkZ)
            } else {
                chunk.reObfuscateChunk()
                for (loader in this.getChunkLoaders(chunkX, chunkZ)) {
                    loader.onChunkChanged(chunk)
                }
            }
        }
    }

    fun getHighestBlockAt(vector: Vector2): Int {
        return getHighestBlockAt(vector.floorX, vector.floorY)
    }

    fun getHighestBlockAt(x: Int, z: Int): Int {
        return getChunk(x shr 4, z shr 4, true).getHeightMap(x and 0x0f, z and 0x0f)
    }

    /** */
    init {
        if (generatorClass == null) {
            throw NullPointerException("Can't find generator for " + generatorConfig.name + " The level " + name + " can't be load!")
        }
        try {
            this.generator =
                generatorClass.getConstructor(DimensionData::class.java, MutableMap::class.java).newInstance(
                    generatorConfig.dimensionData,
                    generatorConfig.preset
                )
            generator.level = (this@Level)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }

        try {
            this.provider = AtomicReference(
                provider.getConstructor(
                    Level::class.java,
                    String::class.java
                ).newInstance(this, path)
            )
        } catch (e: ReflectiveOperationException) {
            throw LevelException("Constructor of $provider failed", e)
        }
        val levelProvider = requireProvider()
        log.info(
            Server.instance.lang.tr(
                "chorus.level.preparing",
                TextFormat.GREEN.toString() + levelProvider.name + TextFormat.RESET
            )
        )
        levelProvider.updateLevelName(name)

        if (generatorConfig.enableAntiXray) {
            this.isAntiXrayEnabled = true
            antiXraySystem!!.reinitAntiXray(false)

            antiXraySystem!!.fakeOreDenominator = (
                    when (generatorConfig.antiXrayMode) {
                        AntiXrayMode.HIGH -> 4
                        AntiXrayMode.MEDIUM -> 8
                        else -> 16
                    }
                    )
            antiXraySystem!!.isPreDeObfuscate = (generatorConfig.preDeobfuscate)
        }

        this.name = name
        this.folderPath = path
        this.time = levelProvider.time.toFloat()

        this.rainTime = requireProvider().rainTime
        if (this.rainTime <= 0) {
            rainTime = ThreadLocalRandom.current().nextInt(168000) + 12000
        }

        this.thunderTime = levelProvider.thunderTime
        if (this.thunderTime <= 0) {
            thunderTime = ThreadLocalRandom.current().nextInt(168000) + 12000
        }

        this.currentTick = levelProvider.currentTick
        this.updateQueue = BlockUpdateScheduler(this, currentTick)

        this.chunkTickRadius =
            Server.instance.viewDistance.coerceAtMost(1.coerceAtLeast(Server.instance.settings.chunkSettings.tickRadius))
        this.chunkGenerationQueueSize = Server.instance.settings.chunkSettings.generationQueueSize
        this.chunksPerTicks = Server.instance.settings.chunkSettings.chunksPerTicks
        this.clearChunksOnTick = Server.instance.settings.chunkSettings.clearTickList
        chunkTickList.clear()
        this.temporalVector = Vector3(0.0, 0.0, 0.0)
        this.scheduler = ServerScheduler()
        this.tickRate = 1

        this.skyLightSubtracted = this.calculateSkylightSubtracted(1f)
        val levelName = getLevelName()
        baseTickGameLoop = GameLoop.builder()
            .onTick { this.doTick(it) }
            .onStop { this.remove() }
            .loopCountPerSec(20)
            .build()
        this.baseTickThread = object : Thread() {
            init {
                setName(this@Level.folderName)
            }

            override fun run() {
                baseTickGameLoop.startLoop()
            }
        }
        subTickGameLoop = GameLoop.builder()
            .onTick(Consumer<GameLoop> { currentTick: GameLoop -> this.subTick(currentTick) })
            .onStop(Runnable { log.debug("$levelName SubTick is closed!") })
            .loopCountPerSec(20)
            .build()
        this.subTickThread = object : Thread() {
            init {
                setName(this@Level.folderName + " SubTick")
            }

            override fun run() {
                subTickGameLoop.startLoop()
            }
        }
    }

    fun getMapColorAt(x: Int, z: Int): BlockColor {
        var color: Color

        val block = getMapColoredBlockAt(x, z) ?: return VOID_BLOCK_COLOR

        //在z轴存在高度差的地方，颜色变深或变浅
        val nzy = getMapColoredBlockAt(x, z - 1) ?: return block.color
        color = block.color.toAwtColor()
        if (nzy.position.floorY > block.position.floorY) {
            color = darker(color, 0.875 - 5.coerceAtMost(nzy.position.floorY - block.position.floorY) * 0.05)
        } else if (nzy.position.floorY < block.position.floorY) {
            color = brighter(color, 0.875 - 5.coerceAtMost(block.position.floorY - nzy.position.floorY) * 0.05)
        }

        //效果不好，暂时禁用
//        var deltaY = block.y - 128;
//        if (deltaY > 0) {
//            color = brighter(color, 1 - deltaY / (192 * 3));
//        } else if (deltaY < 0) {
//            color = darker(color, 1 - (-deltaY) / (192 * 3));
//        }
        val up = block.getSide(BlockFace.UP)
        val up1 = block.getSideAtLayer(1, BlockFace.UP)
        if (up is BlockFlowingWater || up1 is BlockFlowingWater) {
            var r1 = color.red
            var g1 = color.green
            var b1 = color.blue
            //在水下
            if (block.position.y < 62) {
                //在海平面下
                //海平面为62格。离海平面越远颜色越接近海洋颜色
                val depth = 62 - block.position.y
                if (depth > 96) return WATER_BLOCK_COLOR
                b1 = WATER_BLOCK_COLOR.blue
                var radio = (depth / 96.0)
                if (radio < 0.5) radio = 0.5
                r1 = (r1 + (WATER_BLOCK_COLOR.red - r1) * radio).toInt()
                g1 = (g1 + (WATER_BLOCK_COLOR.green - g1) * radio).toInt()
            } else {
                //湖泊 or 河流
                b1 = WATER_BLOCK_COLOR.blue
                r1 = (r1 + (WATER_BLOCK_COLOR.red - r1) * 0.5).toInt()
                g1 = (g1 + (WATER_BLOCK_COLOR.green - g1) * 0.5).toInt()
            }
            color = Color(r1, g1, b1)
        }

        return BlockColor(color.red, color.green, color.blue, color.alpha)
    }

    protected fun brighter(source: Color, factor: Double): Color {
        var r = source.red
        var g = source.green
        var b = source.blue
        val alpha = source.alpha

        val i = (1.0 / (1.0 - factor)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i, alpha)
        }
        if (r > 0 && r < i) r = i
        if (g > 0 && g < i) g = i
        if (b > 0 && b < i) b = i

        return Color(
            Math.min((r / factor).toInt(), 255),
            Math.min((g / factor).toInt(), 255),
            Math.min((b / factor).toInt(), 255),
            alpha
        )
    }

    protected fun darker(source: Color, factor: Double): Color {
        return Color(
            Math.max((source.red * factor).toInt(), 0),
            Math.max((source.green * factor).toInt(), 0),
            Math.max((source.blue * factor).toInt(), 0),
            source.alpha
        )
    }

    protected fun getMapColoredBlockAt(x: Int, z: Int): Block? {
        val chunk = getChunk(x shr 4, z shr 4)
        val chunkX = x and 0xF
        val chunkZ = z and 0xF
        var y = chunk.getHeightMap(chunkX, chunkZ)
        while (y >= minHeight) {
            val block = getBlock(x, y, z)
            if (block.color == BlockColor.VOID_BLOCK_COLOR) return null
            if (block.color.alpha == 0 /* || block instanceof BlockFlowingWater*/) {
                y--
            } else {
                return block
            }
        }
        return null
    }

    fun isChunkLoaded(x: Int, z: Int): Boolean {
        return requireProvider().isChunkLoaded(x, z)
    }

    private fun areNeighboringChunksLoaded(hash: Long): Boolean {
        val levelProvider = this.requireProvider()
        return levelProvider.isChunkLoaded(hash + 1) &&
                levelProvider.isChunkLoaded(hash - 1) &&
                levelProvider.isChunkLoaded(hash + (1L shl 32)) &&
                levelProvider.isChunkLoaded(hash - (1L shl 32))
    }

    fun isChunkGenerated(x: Int, z: Int): Boolean {
        val chunk = this.getChunk(x, z)
        return chunk != null && chunk.isGenerated
    }

    fun isChunkPopulated(x: Int, z: Int): Boolean {
        val chunk = this.getChunk(x, z)
        return chunk != null && chunk.isPopulated
    }

    val spawnLocation: Locator
        get() = Locator.Companion.fromObject(requireProvider().spawn, this)

    fun setSpawnLocation(pos: Vector3) {
        val previousSpawn = this.spawnLocation
        requireProvider().spawn = pos
        Server.instance.pluginManager.callEvent(SpawnChangeEvent(this, previousSpawn))
        getPlayers().values.stream()
            .filter(Predicate<Player> { player: Player -> player.spawn.second == null || player.spawn.second == SpawnPointType.WORLD }
            ).forEach(Consumer<Player> { player: Player ->
                player.setSpawn(
                    spawnLocation, SpawnPointType.WORLD
                )
            })
    }

    val fuzzySpawnLocation: Locator?
        get() {
            var spawn: Locator? = spawnLocation
            val radius = gameRules.getInteger(GameRule.SPAWN_RADIUS)
            if (radius > 0) {
                val random = ThreadLocalRandom.current()
                val negativeFlags = random.nextInt(4)
                spawn = spawn!!.add(
                    radius * random.nextDouble() * (if ((negativeFlags and 1) > 0) -1 else 1),
                    0.0,
                    radius * random.nextDouble() * (if ((negativeFlags and 2) > 0) -1 else 1)
                )
            }
            return spawn
        }

    fun requestChunk(x: Int, z: Int, player: Player) {
        Preconditions.checkArgument(player.loaderId > 0, player.getEntityName() + " has no chunk loader")
        val index = chunkHash(x, z)
        val playerInt2ObjectMap = chunkSendQueue.computeIfAbsent(index) { ConcurrentHashMap<Int, Player>() }
        playerInt2ObjectMap[player.loaderId] = player
    }

    private fun sendChunk(x: Int, z: Int, index: Long, packet: DataPacket) {
        for (player in chunkSendQueue[index]?.values ?: return) {
            if (player.isConnected() && player.usedChunks.contains(index)) {
                player.sendChunk(x, z, packet)
            }
        }
        chunkSendQueue.remove(index)
    }

    fun subTick(currentTick: GameLoop) {
        try {
            processChunkRequest()

            if (currentTick.getTick() % 100 == 0) {
                doLevelGarbageCollection(false)
            }
        } catch (e: Exception) {
            log.error("Subtick Thread for level $folderName failed.", e)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun processChunkRequest() {
        for (index in chunkSendQueue.keys) {
            val x = getHashX(index)
            val z = getHashZ(index)
            val players = chunkSendQueue[index]
            if (players != null) {
                val pair = requireProvider().requestChunkData(x, z)
                for (player in players.values) {
                    if (player.isConnected()) {
                        val ncp = org.chorus_oss.protocol.packets.NetworkChunkPublisherUpdatePacket(
                            position = BlockPos(player.position),
                            radius = (player.viewDistance shl 4).toUInt(),
                            savedChunks = emptyList(),
                        )
                        player.sendPacket(ncp)

                        val pk = org.chorus_oss.protocol.packets.LevelChunkPacket(
                            position = ChunkPos(x, z),
                            dimension = dimensionData.dimensionId,
                            subChunkCount = pair.second.toUInt(),
                            subChunkLimit = 0u,
                            cacheEnabled = false,
                            blobHashes = emptyList(),
                            data = ByteString(pair.first)
                        )
                        player.sendChunk(x, z, pk)
                    }
                }
                chunkSendQueue.remove(index)
            }
        }
    }

    fun sendChunks(player: Player) {
        val chunkPositionX = player.position.chunkX
        val chunkPositionZ = player.position.chunkZ
        val chunkRadius = player.viewDistance

        val ncp = org.chorus_oss.protocol.packets.NetworkChunkPublisherUpdatePacket(
            position = BlockPos(player.position),
            radius = (player.viewDistance shl 4).toUInt(),
            savedChunks = emptyList(),
        )
        player.sendPacket(ncp)

        for (x in -chunkRadius..<chunkRadius) {
            for (z in -chunkRadius..<chunkRadius) {
                val chunkX = chunkPositionX + x
                val chunkZ = chunkPositionZ + z

                val pair = requireProvider().requestChunkData(chunkX, chunkZ)

                val pk = org.chorus_oss.protocol.packets.LevelChunkPacket(
                    position = ChunkPos(chunkX, chunkZ),
                    dimension = dimensionData.dimensionId,
                    subChunkCount = pair.second.toUInt(),
                    subChunkLimit = 0u,
                    cacheEnabled = false,
                    blobHashes = emptyList(),
                    data = ByteString(pair.first)
                )
                player.sendChunk(chunkX, chunkZ, pk)
            }
        }
    }

    fun removeEntity(entity: Entity) {
        if (entity.level !== this) {
            throw LevelException("Invalid Entity level")
        }

        if (entity is Player) {
            players.remove(entity.getRuntimeID())
            playerWeatherShowMap.remove(entity.getEntityName())
            this.checkSleep()
        } else {
            entity.close()
        }

        entities.remove(entity.getRuntimeID())
        updateEntities.remove(entity.getRuntimeID())
    }

    fun addEntity(entity: Entity) {
        if (entity.level !== this) {
            throw LevelException("Invalid Entity level")
        }

        if (entity is Player) {
            players.put(entity.getRuntimeID(), entity)
            playerWeatherShowMap.put(entity.getEntityName(), 0)
        }
        entities.put(entity.getRuntimeID(), entity)
    }

    fun addBlockEntity(blockEntity: BlockEntity) {
        if (blockEntity.level !== this) {
            throw LevelException("Invalid Block Entity level")
        }
        blockEntities.put(blockEntity.id, blockEntity)
    }

    fun scheduleBlockEntityUpdate(entity: BlockEntity) {
        Preconditions.checkNotNull(entity, "entity")
        Preconditions.checkArgument(entity.level === this, "BlockEntity is not in this level")
        if (!updateBlockEntities.contains(entity)) {
            updateBlockEntities.add(entity)
        }
    }

    fun removeBlockEntity(entity: BlockEntity) {
        Preconditions.checkNotNull(entity, "entity")
        Preconditions.checkArgument(entity.level === this, "BlockEntity is not in this level")
        blockEntities.remove(entity.id)
        updateBlockEntities.remove(entity)
    }

    /**
     * 该区块是否在使用中，出生点区块，tick区域中的区块，以及存在[ChunkLoader]的区块都被看做正在使用
     *
     *
     * Whether the chunk is in use, spawn chunks, chunks in the tick area, and chunks with [ChunkLoader] are considered in use
     *
     * @param x the chunk x
     * @param z the chunk z
     * @return the boolean
     */
    fun isChunkInUse(x: Int, z: Int): Boolean {
        return isChunkInUse(chunkHash(x, z))
    }

    /**
     * 该区块是否在使用中，出生点区块，tick区域中的区块，以及存在[ChunkLoader]的区块都被看做正在使用
     *
     *
     * Whether the chunk is in use, spawn chunks, chunks in the tick area, and chunks with [ChunkLoader] are considered in use
     *
     * @param hash chunk hash value from [.chunkHash]
     * @return the boolean
     */
    fun isChunkInUse(hash: Long): Boolean {
        if (isSpawnChunk(getHashX(hash), getHashZ(hash))) {
            return true
        }

        val tickingAreaManager = Server.instance.tickingAreaManager
        if (tickingAreaManager.getTickingAreaByChunk(
                this.getLevelName(),
                TickingArea.ChunkPos(getHashX(hash), getHashZ(hash))
            ) != null
        ) {
            return true
        }
        val integerChunkLoaderMap = chunkLoaders[hash]
        return if (integerChunkLoaderMap != null) {
            chunkLoaders.containsKey(hash) && integerChunkLoaderMap.isNotEmpty()
        } else false
    }

    fun getChunk(chunkX: Int, chunkZ: Int): IChunk {
        return this.getChunk(chunkX, chunkZ, false)
    }

    fun getChunk(pos: ChunkVector2): IChunk {
        return getChunk(pos.x, pos.z, false)
    }

    fun getChunk(chunkX: Int, chunkZ: Int, create: Boolean): IChunk {
        val index = chunkHash(chunkX, chunkZ)
        var chunk = requireProvider().getLoadedChunk(index)
        if (chunk == null) {
            chunk = this.forceLoadChunk(index, chunkX, chunkZ, create)
        }
        return chunk
    }

    fun getChunkAsync(chunkX: Int, chunkZ: Int): CompletableFuture<IChunk?> {
        return this.getChunkAsync(chunkX, chunkZ, false)
    }

    fun getChunkAsync(pos: ChunkVector2): CompletableFuture<IChunk?> {
        return getChunkAsync(pos.x, pos.z, false)
    }

    fun getChunkAsync(chunkX: Int, chunkZ: Int, create: Boolean): CompletableFuture<IChunk?> {
        return CompletableFuture.supplyAsync<IChunk?>({
            val index = chunkHash(chunkX, chunkZ)
            var chunk = requireProvider().getLoadedChunk(index)
            if (chunk == null) {
                chunk = this.forceLoadChunk(index, chunkX, chunkZ, create)
            }
            chunk
        }, this.scheduler.asyncTaskThreadPool)
    }


    @JvmOverloads
    fun loadChunk(x: Int, z: Int, generate: Boolean = true): Boolean {
        val index = chunkHash(x, z)
        if (requireProvider().isChunkLoaded(index)) {
            return true
        }
        return forceLoadChunk(index, x, z, generate) != null
    }

    private fun forceLoadChunk(index: Long, x: Int, z: Int, generate: Boolean): IChunk {
        val chunk = requireProvider().getChunk(x, z, generate)
        if (chunk == null && !generate) {
            throw IllegalStateException("Could not create new Chunk")
        }

        Server.instance.pluginManager.callEvent(ChunkLoadEvent(chunk!!, !chunk.isGenerated))
        chunk.initChunk()

        if (this.isChunkInUse(index)) {
            unloadQueue.remove(index)
            for (loader in this.getChunkLoaders(x, z)) {
                loader.onChunkLoaded(chunk)
            }
        } else {
            unloadQueue.put(index, System.currentTimeMillis())
        }
        return chunk
    }

    private fun queueUnloadChunk(x: Int, z: Int) {
        val index = chunkHash(x, z)
        unloadQueue.put(index, System.currentTimeMillis())
    }

    /**
     * submit a unload chunk request.
     *
     * @param x    the x
     * @param z    the z
     * @param safe if true,will check the chunk whether is used
     * @return whether the request commit was successful
     */
    @JvmOverloads
    fun unloadChunkRequest(x: Int, z: Int, safe: Boolean = true): Boolean {
        if (safe && this.isChunkInUse(x, z)) {
            return false
        }

        this.queueUnloadChunk(x, z)

        return true
    }

    fun cancelUnloadChunkRequest(x: Int, z: Int) {
        this.cancelUnloadChunkRequest(chunkHash(x, z))
    }

    fun cancelUnloadChunkRequest(hash: Long) {
        unloadQueue.remove(hash)
    }

    @JvmOverloads
    fun unloadChunk(x: Int, z: Int, safe: Boolean = true): Boolean {
        return this.unloadChunk(x, z, safe, true)
    }

    /**
     * Unload chunk from memory
     *
     * @param safe    check the chunk if is used
     * @param trySave Whether to try to save the chunk
     */
    @Synchronized
    fun unloadChunk(x: Int, z: Int, safe: Boolean, trySave: Boolean): Boolean {
        if (safe && this.isChunkInUse(x, z)) {
            return false
        }

        if (!this.isChunkLoaded(x, z)) {
            return true
        }

        val chunk = this.getChunk(x, z)

        if (chunk != null && chunk.provider != null) {
            val ev: ChunkUnloadEvent = ChunkUnloadEvent(chunk)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            }
        }

        try {
            val levelProvider = this.requireProvider()
            if (chunk != null) {
                if (trySave && this.autoSave) {
                    var entities = 0
                    for (e in chunk.entities.values) {
                        if (e is Player) {
                            continue
                        }
                        ++entities
                    }

                    if (chunk.hasChanged() || !chunk.blockEntities.isEmpty() || entities > 0) {
                        levelProvider.setChunk(x, z, chunk)
                        levelProvider.saveChunk(x, z)
                    }
                }
                for (loader in this.getChunkLoaders(x, z)) {
                    loader.onChunkUnloaded(chunk)
                }
            }
            levelProvider.unloadChunk(x, z, safe)
        } catch (e: Exception) {
            log.error(Server.instance.lang.tr("chorus.level.chunkUnloadError", e.toString()), e)
        }

        return true
    }

    fun isSpawnChunk(x: Int, z: Int): Boolean {
        val spawn = requireProvider().spawn
        val spawnCX = spawn.floorX shr 4
        val spawnCZ = spawn.floorZ shr 4
        return x == spawnCX && z == spawnCZ
    }

    val safeSpawn: Locator
        get() = getSafeSpawn(null)

    fun getSafeSpawn(spawn: Vector3?): Locator {
        return getSafeSpawn(spawn, Server.instance.settings.playerSettings.spawnRadius)
    }

    fun getSafeSpawn(spawn: Vector3?, horizontalMaxOffset: Int): Locator {
        val safe = getSafeSpawn(spawn, horizontalMaxOffset, true)
        return safe
    }

    fun getSafeSpawn(spawn: Vector3?, horizontalMaxOffset: Int, allowWaterUnder: Boolean): Locator {
        var spawn1 = spawn
        if (spawn1 == null) spawn1 = fuzzySpawnLocation!!.position
        if (standable(spawn1, true)) return Locator.fromObject(spawn1, this)

        val maxY = if (isNether) 127 else (if (isOverWorld) 319 else 255)
        val minY = if (isOverWorld) -64 else 0

        for (horizontalOffset in 0..horizontalMaxOffset) {
            for (y in maxY downTo minY) {
                val pos = Locator.fromObject(spawn1, this)
                pos.position.y = (y.toDouble())

                var newSpawn: Locator
                if (standable(
                        pos.add(horizontalOffset.toDouble(), 0.0, horizontalOffset.toDouble())
                            .also { newSpawn = it }.position,
                        allowWaterUnder
                    )
                ) return newSpawn

                if (standable(
                        pos.add(horizontalOffset.toDouble(), 0.0, -horizontalOffset.toDouble())
                            .also { newSpawn = it }.position,
                        allowWaterUnder
                    )
                ) return newSpawn

                if (standable(
                        pos.add(-horizontalOffset.toDouble(), 0.0, horizontalOffset.toDouble())
                            .also { newSpawn = it }.position,
                        allowWaterUnder
                    )
                ) return newSpawn

                if (standable(
                        pos.add(-horizontalOffset.toDouble(), 0.0, -horizontalOffset.toDouble())
                            .also { newSpawn = it }.position,
                        allowWaterUnder
                    )
                ) return newSpawn
            }
        }

        log.warn("cannot find a safe spawn around " + spawn1.asBlockVector3() + "!")
        return Locator.fromObject(spawn1, this)
    }

    @JvmOverloads
    fun standable(vec: Vector3, allowWaterUnder: Boolean = false): Boolean {
        val pos: Locator = Locator.fromObject(vec, this)
        val blockUnder = pos.add(0.0, -1.0, 0.0).getLevelBlock(0, true)
        val block = pos.getLevelBlock(0, true)
        val blockUpper = pos.add(0.0, 1.0, 0.0).getLevelBlock(0, true)
        return if (!allowWaterUnder) !blockUnder.canPassThrough() && (block.isAir || block.canPassThrough())
                && (blockUpper.isAir || block.canPassThrough())
        else (!blockUnder.canPassThrough() || blockUnder is BlockFlowingWater)
                && (block.isAir || block.canPassThrough())
                && (blockUpper.isAir || block.canPassThrough())
    }

    val isTicked: Boolean
        get() {
            return if (Server.instance.settings.levelSettings.levelThread) {
                baseTickGameLoop.isRunning()
            } else Server.instance.levels.containsKey(this.id)
        }

    val isThreadRunning: Boolean
        get() = baseTickThread.isAlive

    /**
     * 获取这个地图经历的时间(一直会累加)
     *
     *
     * Get the elapsed time for this level
     */
    fun getTime(): Int {
        return time.toInt()
    }

    val dayTime: Int
        get() = this.getTime() % TIME_FULL

    val isDay: Boolean
        get() = (dayTime < 13184 || dayTime > 22800)

    val isNight: Boolean
        get() = (dayTime > 13184 && dayTime < 22800)

    /**
     * 设置这个地图经历的时间
     *
     *
     * Set the elapsed time for this level
     */
    fun setTime(time: Int) {
        if (isRaining) {
            if (getTime() % TIME_FULL != time % TIME_FULL) {
                //Day changed
                setRaining(false)
                setThundering(false)
            }
        }
        this.time = time.toFloat()
        this.sendTime()
    }

    val isDaytime: Boolean
        get() = this.skyLightSubtracted < 4

    fun getLevelName(): String {
        return requireProvider().name
    }

    val folderName: String
        get() = File(folderPath).name

    fun stopTime() {
        this.stopTime = true
        this.sendTime()
    }

    fun startTime() {
        this.stopTime = false
        this.sendTime()
    }

    val seed: Long
        get() = requireProvider().seed

    fun setSeed(seed: Int) {
        requireProvider().seed = seed.toLong()
    }


    fun regenerateChunk(x: Int, z: Int) {
        val chunkLoadersCopy = getChunkLoaders(x, z)
        val chunk = requireProvider().getChunk(x, z)
        chunk!!.chunkState = ChunkState.NEW
        this.unloadChunk(x, z, false)
        this.cancelUnloadChunkRequest(x, z)
        for (loader in chunkLoadersCopy) {
            if (loader is Player) {
                loader.onChunkChanged(chunk)
            }
        }
    }

    fun syncRegenerateChunk(x: Int, z: Int) {
        val chunkLoadersCopy = getChunkLoaders(x, z)
        val chunk = requireProvider().getChunk(x, z)
        chunk!!.chunkState = ChunkState.NEW
        this.unloadChunk(x, z, false)
        this.cancelUnloadChunkRequest(x, z)

        val levelProvider = requireProvider()
        syncGenerateChunk(x, z)
        levelProvider.setChunk(x, z, getChunk(x, z))

        for (loader in chunkLoadersCopy) {
            if (loader is Player) {
                loader.onChunkChanged(chunk)
            }
        }
    }

    @JvmOverloads
    fun generateChunk(x: Int, z: Int, force: Boolean = false) {
        if (chunkGenerationQueue.size >= this.chunkGenerationQueueSize && !force) {
            return
        }
        val index = chunkHash(x, z)
        if (chunkGenerationQueue.putIfAbsent(index, java.lang.Boolean.TRUE) == null) {
            val chunk = this.getChunk(x, z, true)
            generator.asyncGenerate(
                chunk
            ) { c: ChunkGenerateContext -> chunkGenerationQueue.remove(c.chunk.index) } //async
        }
    }

    fun syncGenerateChunk(x: Int, z: Int) {
        val index = chunkHash(x, z)
        if (chunkGenerationQueue.putIfAbsent(index, java.lang.Boolean.TRUE) == null) {
            val chunk = this.getChunk(x, z, true)
            generator.syncGenerate(chunk)
            chunkGenerationQueue.remove(index)
        }
    }

    /**
     * 异步执行服务器内存垃圾收集
     *
     *
     * Run server memory garbage collection asynchronously
     *
     * @return the list
     */
    fun doLevelGarbageCollection(force: Boolean) {
        // remove all invaild block entities.
        if (!blockEntities.isEmpty()) {
            val iter = blockEntities.values.iterator()
            while (iter.hasNext()) {
                iter.next()
            }
        }

        //gcDeadChunks
        for (entry in requireProvider().loadedChunks.entries) {
            val index: Long = entry.key
            if (!unloadQueue.containsKey(index)) {
                val chunk: IChunk = entry.value
                this.unloadChunkRequest(chunk.x, chunk.z, true)
            }
        }
        this.unloadChunks()
    }

    @JvmOverloads
    fun unloadChunks(force: Boolean = false) {
        unloadChunks(96, force)
    }

    fun unloadChunks(maxUnload: Int, force: Boolean) {
        var maxUnload1 = maxUnload
        if (!unloadQueue.isEmpty()) {
            val now = System.currentTimeMillis()

            var toRemove: MutableList<Long>? = mutableListOf()
            for (entry in unloadQueue.entries) {
                val index = entry.key
                if (isChunkInUse(index)) {
                    continue
                }
                if (!force) {
                    val time: Long = entry.value
                    if (maxUnload1 <= 0) {
                        break
                    } else if (time > (now - Server.instance.settings.levelSettings.chunkUnloadDelay)
                    ) {
                        continue
                    }
                }

                if (toRemove == null) toRemove = mutableListOf()
                toRemove.add(index)
            }

            if (toRemove != null) {
                val size: Int = toRemove.size
                for (i in 0..<size) {
                    val index = toRemove[i]
                    val X = getHashX(index)
                    val Z = getHashZ(index)

                    if (this.unloadChunk(X, Z, true)) {
                        unloadQueue.remove(index)
                        --maxUnload1
                    }
                }
            }
        }
    }

    fun setRaining(raining: Boolean): Boolean {
        val ev = WeatherChangeEvent(this, raining)
        Server.instance.pluginManager.callEvent(ev)

        if (ev.cancelled) {
            return false
        }

        this.isRaining = raining

        val pk = LevelEventPacket()
        if (raining) {
            pk.evid = LevelEventPacket.EVENT_START_RAINING
            val time = ThreadLocalRandom.current().nextInt(12000) + 12000 // These numbers are from Minecraft
            pk.data = time
            rainTime = time
        } else {
            pk.evid = LevelEventPacket.EVENT_STOP_RAINING
            rainTime = ThreadLocalRandom.current().nextInt(168000) + 12000
        }

        for (p in getPlayers().values) {
            playerWeatherShowMap.put(p.getEntityName(), if (raining) 1 else 0)
            p.dataPacket(pk)
        }

        return true
    }


    fun isThundering(): Boolean {
        return isRaining && this.thundering
    }

    fun setThundering(thundering: Boolean): Boolean {
        val ev: ThunderChangeEvent = ThunderChangeEvent(this, thundering)
        Server.instance.pluginManager.callEvent(ev)

        if (ev.cancelled) {
            return false
        }

        if (thundering && !isRaining) {
            setRaining(true)
        }

        this.thundering = thundering

        val pk = LevelEventPacket()
        // These numbers are from Minecraft
        if (thundering) {
            pk.evid = LevelEventPacket.EVENT_START_THUNDERSTORM
            val time = ThreadLocalRandom.current().nextInt(12000) + 3600
            pk.data = time
            thunderTime = time
        } else {
            pk.evid = LevelEventPacket.EVENT_STOP_THUNDERSTORM
            thunderTime = ThreadLocalRandom.current().nextInt(168000) + 12000
        }

        for (p in getPlayers().values) {
            playerWeatherShowMap.put(p.getEntityName(), if (isRaining) 2 else 0)
            p.dataPacket(pk)
        }

        return true
    }

    fun sendWeather(players: Array<Player>?) {
        var players1 = players
        if (players1 == null) {
            players1 = getPlayers().values.toTypedArray()
        }

        val pk = LevelEventPacket()

        if (this.isRaining) {
            pk.evid = LevelEventPacket.EVENT_START_RAINING
            pk.data = this.rainTime
        } else {
            pk.evid = LevelEventPacket.EVENT_STOP_RAINING
        }

        Server.broadcastPacket(players1, pk)

        if (this.isThundering()) {
            pk.evid = LevelEventPacket.EVENT_START_THUNDERSTORM
            pk.data = this.thunderTime
        } else {
            pk.evid = LevelEventPacket.EVENT_STOP_THUNDERSTORM
        }

        Server.broadcastPacket(players1, pk)
    }

    fun sendWeather(player: Player?) {
        if (player != null) {
            this.sendWeather(arrayOf(player))
        }
    }

    fun sendWeather(players: Collection<Player>?) {
        var players1 = players
        if (players1 == null) {
            players1 = getPlayers().values
        }
        this.sendWeather(players1.toTypedArray())
    }

    val dimensionData: DimensionData
        get() = generator.dimensionData

    val dimension: Int
        get() = dimensionData.dimensionId

    val isOverWorld: Boolean
        get() = dimension == 0

    val isNether: Boolean
        get() = dimension == 1

    val isTheEnd: Boolean
        get() = dimension == 2

    fun isYInRange(y: Int): Boolean {
        return y >= minHeight && y <= maxHeight
    }

    fun canBlockSeeSky(pos: Vector3): Boolean {
        return this.getHighestBlockAt(pos.floorX, pos.floorZ) < pos.y
    }

    val minHeight: Int
        /**
         * Minimum height where blocks can be placed
         */
        get() = dimensionData.minHeight

    val maxHeight: Int
        /**
         * The maximum height at which blocks can be placed
         */
        get() = dimensionData.maxHeight

    fun getStrongPower(pos: IVector3, direction: BlockFace): Int {
        return getBlock(pos.vector3).getStrongPower(direction)
    }

    fun getStrongPower(pos: IVector3): Int {
        if (pos is BlockPistonBase || getBlock(pos.vector3) is BlockPistonBase) return 0

        var i = 0
        for (face in BlockFace.entries) {
            i = Math.max(
                i,
                this.getStrongPower(temporalVector.setComponentsAdding(pos.vector3, face), face)
            )

            if (i >= 15) {
                return i
            }
        }

        return i
    }

    fun isSidePowered(pos: IVector3, face: BlockFace): Boolean {
        return this.getRedstonePower(pos, face) > 0
    }

    /**
     * Get the block redstone power can output.
     *
     * @param pos  the block pos
     * @param face Only be used on block with not isNormalBlock, such as redstone torch
     */
    fun getRedstonePower(pos: IVector3, face: BlockFace): Int {
        val block = if (pos is Block) {
            pos
        } else {
            getBlock(pos.vector3)
        }

        return if (block.isNormalBlock) this.getStrongPower(pos) else block.getWeakPower(face)
    }

    fun isBlockPowered(pos: Vector3): Boolean {
        for (face in BlockFace.entries) {
            if (this.getRedstonePower(temporalVector.setComponentsAdding(pos, face), face) > 0) {
                return true
            }
        }

        return false
    }

    fun isBlockIndirectlyGettingPowered(pos: Vector3): Int {
        var power = 0

        for (face in BlockFace.entries) {
            val blockPower = this.getRedstonePower(temporalVector.setComponentsAdding(pos, face), face)

            if (blockPower >= 15) {
                return 15
            }

            if (blockPower > power) {
                power = blockPower
            }
        }

        return power
    }

    fun isAreaLoaded(bb: AxisAlignedBB): Boolean {
        if (bb.maxY < (if (isOverWorld) -64 else 0) || bb.minY >= (if (isOverWorld) 320 else 256)) {
            return false
        }
        val minX = floor(bb.minX).toInt() shr 4
        val minZ = floor(bb.minZ).toInt() shr 4
        val maxX = floor(bb.maxX).toInt() shr 4
        val maxZ = floor(bb.maxZ).toInt() shr 4

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                if (!this.isChunkLoaded(x, z)) {
                    return false
                }
            }
        }

        return true
    }

    fun createPortal(target: Block): Boolean {
        if (this.dimension == DIMENSION_THE_END) return false
        val maxPortalSize = 23
        val targX = target.position.floorX
        val targY = target.position.floorY
        val targZ = target.position.floorZ
        //check if there's air above (at least 3 blocks)
        for (i in 1..3) {
            if (!getBlock(targX, targY + i, targZ).isAir) {
                return false
            }
        }
        var sizePosX = 0
        var sizeNegX = 0
        var sizePosZ = 0
        var sizeNegZ = 0
        for (i in 1..<maxPortalSize) {
            if (getBlock(targX + i, targY, targZ).id == BlockID.OBSIDIAN) {
                sizePosX++
            } else {
                break
            }
        }
        for (i in 1..<maxPortalSize) {
            if (getBlock(targX - i, targY, targZ).id == BlockID.OBSIDIAN) {
                sizeNegX++
            } else {
                break
            }
        }
        for (i in 1..<maxPortalSize) {
            if (getBlock(targX, targY, targZ + i).id == BlockID.OBSIDIAN) {
                sizePosZ++
            } else {
                break
            }
        }
        for (i in 1..<maxPortalSize) {
            if (getBlock(targX, targY, targZ - i).id == BlockID.OBSIDIAN) {
                sizeNegZ++
            } else {
                break
            }
        }
        //plus one for target block
        val sizeX = sizePosX + sizeNegX + 1
        val sizeZ = sizePosZ + sizeNegZ + 1
        if (sizeX >= 2 && sizeX <= maxPortalSize) {
            //start scan from 1 block above base
            //find pillar or end of portal to start scan
            var scanX = targX
            val scanY = targY + 1
            val scanZ = targZ
            for (i in 0..<sizePosX + 1) {
                //this must be air
                if (!getBlock(scanX + i, scanY, scanZ).isAir) {
                    return false
                }
                if (getBlock(scanX + i + 1, scanY, scanZ).id == BlockID.OBSIDIAN) {
                    scanX += i
                    break
                }
            }
            //make sure that the above loop finished
            if (getBlock(scanX + 1, scanY, scanZ).id != BlockID.OBSIDIAN) {
                return false
            }

            var innerWidth = 0
            LOOP@ for (i in 0..<maxPortalSize - 2) {
                val id = getBlock(scanX - i, scanY, scanZ).id
                when (id) {
                    BlockID.AIR -> innerWidth++
                    BlockID.OBSIDIAN -> break@LOOP
                    else -> return false
                }
            }
            var innerHeight = 0
            LOOP@ for (i in 0..<maxPortalSize - 2) {
                val id = getBlock(scanX, scanY + i, scanZ).id
                when (id) {
                    BlockID.AIR -> innerHeight++
                    BlockID.OBSIDIAN -> break@LOOP
                    else -> return false
                }
            }
            if (!(innerWidth <= maxPortalSize - 2 && innerWidth >= 2 && innerHeight <= maxPortalSize - 2 && innerHeight >= 3)) {
                return false
            }

            for (height in 0..<innerHeight + 1) {
                if (height == innerHeight) {
                    for (width in 0..<innerWidth) {
                        if (getBlock(scanX - width, scanY + height, scanZ).id != BlockID.OBSIDIAN) {
                            return false
                        }
                    }
                } else {
                    if (getBlock(
                            scanX + 1,
                            scanY + height,
                            scanZ
                        ).id != BlockID.OBSIDIAN || getBlock(scanX - innerWidth, scanY + height, scanZ)
                            .id != BlockID.OBSIDIAN
                    ) {
                        return false
                    }

                    for (width in 0..<innerWidth) {
                        if (!getBlock(scanX - width, scanY + height, scanZ).isAir) {
                            return false
                        }
                    }
                }
            }

            for (height in 0..<innerHeight) {
                for (width in 0..<innerWidth) {
                    this.setBlock(
                        Vector3((scanX - width).toDouble(), (scanY + height).toDouble(), scanZ.toDouble()),
                        Block.get(BlockID.PORTAL)
                    )
                }
            }

            this.addSound(target.position, Sound.FIRE_IGNITE)
            return true
        } else if (sizeZ >= 2 && sizeZ <= maxPortalSize) {
            //start scan from 1 block above base
            //find pillar or end of portal to start scan
            val scanX = targX
            val scanY = targY + 1
            var scanZ = targZ
            for (i in 0..<sizePosZ + 1) {
                //this must be air
                if (!getBlock(scanX, scanY, scanZ + i).isAir) {
                    return false
                }
                if (getBlock(scanX, scanY, scanZ + i + 1).id == BlockID.OBSIDIAN) {
                    scanZ += i
                    break
                }
            }
            //make sure that the above loop finished
            if (getBlock(scanX, scanY, scanZ + 1).id != BlockID.OBSIDIAN) {
                return false
            }

            var innerWidth = 0
            LOOP@ for (i in 0..<maxPortalSize - 2) {
                val id = getBlock(scanX, scanY, scanZ - i).id
                when (id) {
                    BlockID.AIR -> innerWidth++
                    BlockID.OBSIDIAN -> break@LOOP
                    else -> return false
                }
            }
            var innerHeight = 0
            LOOP@ for (i in 0..<maxPortalSize - 2) {
                val id = getBlock(scanX, scanY + i, scanZ).id
                when (id) {
                    BlockID.AIR -> innerHeight++
                    BlockID.OBSIDIAN -> break@LOOP
                    else -> return false
                }
            }
            if (!(innerWidth <= maxPortalSize - 2 && innerWidth >= 2 && innerHeight <= maxPortalSize - 2 && innerHeight >= 3)) {
                return false
            }

            for (height in 0..<innerHeight + 1) {
                if (height == innerHeight) {
                    for (width in 0..<innerWidth) {
                        if (getBlock(scanX, scanY + height, scanZ - width).id != BlockID.OBSIDIAN) {
                            return false
                        }
                    }
                } else {
                    if (getBlock(scanX, scanY + height, scanZ + 1).id != BlockID.OBSIDIAN || getBlock(
                            scanX,
                            scanY + height,
                            scanZ - innerWidth
                        )
                            .id != BlockID.OBSIDIAN
                    ) {
                        return false
                    }

                    for (width in 0..<innerWidth) {
                        if (!getBlock(scanX, scanY + height, scanZ - width).isAir) {
                            return false
                        }
                    }
                }
            }

            for (height in 0..<innerHeight) {
                for (width in 0..<innerWidth) {
                    this.setBlock(
                        Vector3(scanX.toDouble(), (scanY + height).toDouble(), (scanZ - width).toDouble()),
                        Block.get(BlockID.PORTAL)
                    )
                }
            }

            this.addSound(target.position, Sound.FIRE_IGNITE)
            return true
        }

        return false
    }

    fun isRayCollidingWithBlocks(
        srcX: Double,
        srcY: Double,
        srcZ: Double,
        dstX: Double,
        dstY: Double,
        dstZ: Double,
        stepSize: Double
    ): Boolean {
        val direction = Vector3(dstX - srcX, dstY - srcY, dstZ - srcZ)
        if (direction.x == 0.0 && direction.y == 0.0 && direction.z == 0.0) {
            return false
        }

        val length = direction.length()
        val normalizedDirection = direction.divide(length)

        var t = 0.0
        while (t < length) {
            val x = Math.round(srcX + normalizedDirection.x * t).toInt()
            val y = Math.round(srcY + normalizedDirection.y * t).toInt()
            val z = Math.round(srcZ + normalizedDirection.z * t).toInt()

            val block = getBlock(x, y, z)
            if (block.collisionBoundingBox != null) {
                val bb: AxisAlignedBB = block.collisionBoundingBox!!
                if (bb.isVectorInside(x.toDouble(), y.toDouble(), z.toDouble())) {
                    return true
                }
            }
            t += stepSize
        }

        return false // No collision with any blocks
    }

    fun getBlockDensity(source: Vector3, boundingBox: AxisAlignedBB): Float {
        val diffX: Double = boundingBox.maxX - boundingBox.minX
        val diffY: Double = boundingBox.maxY - boundingBox.minY
        val diffZ: Double = boundingBox.maxZ - boundingBox.minZ
        val xInterval = 1 / (diffX * 2 + 1)
        val yInterval = 1 / (diffY * 2 + 1)
        val zInterval = 1 / (diffZ * 2 + 1)

        if (xInterval < 0.0 || yInterval < 0.0 || zInterval < 0.0) {
            return 0.0f
        }

        val xOffset = (1 - Math.floor(1 / xInterval) * xInterval) / 2
        val yOffset: Double = boundingBox.minY
        val zOffset = (1 - Math.floor(1 / zInterval) * zInterval) / 2

        var visibleBlocks = 0
        var totalBlocks = 0

        var x = 0f
        while (x <= 1) {
            val fromX = Math.fma(x.toDouble(), diffX, xOffset)
            var y = 0f
            while (y <= 1) {
                val fromY = Math.fma(y.toDouble(), diffY, yOffset)
                var z = 0f
                while (z <= 1) {
                    totalBlocks++
                    val fromZ = Math.fma(z.toDouble(), diffZ, zOffset)

                    if (this.isRayCollidingWithBlocks(source.x, source.y, source.z, fromX, fromY, fromZ, 0.3)) {
                        visibleBlocks++
                    }
                    z = (z.toDouble() + zInterval).toFloat()
                }
                y = (y.toDouble() + yInterval).toFloat()
            }
            x = (x.toDouble() + xInterval).toFloat()
        }

        return visibleBlocks.toFloat() / totalBlocks.toFloat()
    }

    fun ensureY(y: Int): Int {
        return Math.max(Math.min(y, dimensionData.maxHeight), dimensionData.minHeight)
    }

    override fun toString(): String {
        return "Level{" +
                "name='" + name + '\'' +
                ", dimension=" + dimension +
                '}'
    }


    private data class QueuedUpdate(
        val block: Block,
        val neighbor: BlockFace,
    )

    companion object {
        // region finals - number finals
        @JvmField
        val EMPTY_ARRAY: Array<Level> = arrayOf()
        const val BLOCK_UPDATE_NORMAL: Int = 1
        const val BLOCK_UPDATE_RANDOM: Int = 2
        const val BLOCK_UPDATE_SCHEDULED: Int = 3
        const val BLOCK_UPDATE_WEAK: Int = 4
        const val BLOCK_UPDATE_TOUCH: Int = 5
        const val BLOCK_UPDATE_REDSTONE: Int = 6
        const val BLOCK_UPDATE_TICK: Int = 7

        @JvmField
        val BLOCK_UPDATE_MOVED: Int = Utils.dynamic(1000000)
        const val TIME_DAY: Int = 0
        const val TIME_NOON: Int = 6000
        const val TIME_SUNSET: Int = 12000
        const val TIME_NIGHT: Int = 14000
        const val TIME_MIDNIGHT: Int = 18000
        const val TIME_SUNRISE: Int = 23000
        const val TIME_FULL: Int = 24000
        const val DIMENSION_OVERWORLD: Int = 0
        const val DIMENSION_NETHER: Int = 1
        const val DIMENSION_THE_END: Int = 2
        const val DIMENSION_UNDEFINED: Int = 3
        const val MAX_BLOCK_CACHE: Int = 512 // Lower values use less memory
        private const val LCG_CONSTANT = 1013904223
        var COMPRESSION_LEVEL: Int = 8
        private var levelIdCounter = 1
        private var chunkLoaderCounter = 1

        // endregion finals - number finals
        private val randomTickBlocks: MutableSet<String> = HashSet(64) // The blocks that can randomly tick

        init {
            randomTickBlocks.add(BlockID.GRASS_BLOCK)
            randomTickBlocks.add(BlockID.FARMLAND)
            randomTickBlocks.add(BlockID.MYCELIUM)
            randomTickBlocks.add(BlockID.ACACIA_SAPLING)
            randomTickBlocks.add(BlockID.CHERRY_SAPLING)
            randomTickBlocks.add(BlockID.SPRUCE_SAPLING)
            randomTickBlocks.add(BlockID.BAMBOO_SAPLING)
            randomTickBlocks.add(BlockID.OAK_SAPLING)
            randomTickBlocks.add(BlockID.JUNGLE_SAPLING)
            randomTickBlocks.add(BlockID.DARK_OAK_SAPLING)
            randomTickBlocks.add(BlockID.BIRCH_SAPLING)
            randomTickBlocks.add(BlockID.ACACIA_LEAVES)
            randomTickBlocks.add(BlockID.AZALEA_LEAVES)
            randomTickBlocks.add(BlockID.BIRCH_LEAVES)
            randomTickBlocks.add(BlockID.AZALEA_LEAVES_FLOWERED)
            randomTickBlocks.add(BlockID.CHERRY_LEAVES)
            randomTickBlocks.add(BlockID.DARK_OAK_LEAVES)
            randomTickBlocks.add(BlockID.JUNGLE_LEAVES)
            randomTickBlocks.add(BlockID.MANGROVE_LEAVES)
            randomTickBlocks.add(BlockID.OAK_LEAVES)
            randomTickBlocks.add(BlockID.SPRUCE_LEAVES)
            randomTickBlocks.add(BlockID.SNOW_LAYER)
            randomTickBlocks.add(BlockID.ICE)
            randomTickBlocks.add(BlockID.FLOWING_LAVA)
            randomTickBlocks.add(BlockID.LAVA)
            randomTickBlocks.add(BlockID.CACTUS)
            randomTickBlocks.add(BlockID.BEETROOT)
            randomTickBlocks.add(BlockID.CARROTS)
            randomTickBlocks.add(BlockID.POTATOES)
            randomTickBlocks.add(BlockID.MELON_STEM)
            randomTickBlocks.add(BlockID.PUMPKIN_STEM)
            randomTickBlocks.add(BlockID.WHEAT)
            randomTickBlocks.add(BlockID.REEDS)
            randomTickBlocks.add(BlockID.RED_MUSHROOM)
            randomTickBlocks.add(BlockID.BROWN_MUSHROOM)
            randomTickBlocks.add(BlockID.NETHER_WART)
            randomTickBlocks.add(BlockID.FIRE)
            randomTickBlocks.add(BlockID.LIT_REDSTONE_ORE)
            randomTickBlocks.add(BlockID.COCOA)
            randomTickBlocks.add(BlockID.VINE)
            randomTickBlocks.add(BlockID.BRAIN_CORAL_FAN)
            randomTickBlocks.add(BlockID.BUBBLE_CORAL_FAN)
            randomTickBlocks.add(BlockID.DEAD_BRAIN_CORAL_FAN)
            randomTickBlocks.add(BlockID.DEAD_BUBBLE_CORAL_FAN)
            randomTickBlocks.add(BlockID.DEAD_FIRE_CORAL_FAN)
            randomTickBlocks.add(BlockID.DEAD_HORN_CORAL_FAN)
            randomTickBlocks.add(BlockID.DEAD_TUBE_CORAL_FAN)
            randomTickBlocks.add(BlockID.FIRE_CORAL_FAN)
            randomTickBlocks.add(BlockID.HORN_CORAL_FAN)
            randomTickBlocks.add(BlockID.TUBE_CORAL_FAN)
            randomTickBlocks.add(BlockID.KELP)
            randomTickBlocks.add(BlockID.SWEET_BERRY_BUSH)
            randomTickBlocks.add(BlockID.TURTLE_EGG)
            randomTickBlocks.add(BlockID.BAMBOO)
            randomTickBlocks.add(BlockID.CRIMSON_NYLIUM)
            randomTickBlocks.add(BlockID.WARPED_NYLIUM)
            randomTickBlocks.add(BlockID.TWISTING_VINES)
            randomTickBlocks.add(BlockID.CHORUS_FLOWER)
            randomTickBlocks.add(BlockID.COPPER_BLOCK)
            randomTickBlocks.add(BlockID.EXPOSED_COPPER)
            randomTickBlocks.add(BlockID.WEATHERED_COPPER)
            randomTickBlocks.add(BlockID.WAXED_COPPER)
            randomTickBlocks.add(BlockID.CUT_COPPER)
            randomTickBlocks.add(BlockID.EXPOSED_CUT_COPPER)
            randomTickBlocks.add(BlockID.WEATHERED_CUT_COPPER)
            randomTickBlocks.add(BlockID.CUT_COPPER_STAIRS)
            randomTickBlocks.add(BlockID.EXPOSED_CUT_COPPER_STAIRS)
            randomTickBlocks.add(BlockID.WEATHERED_CUT_COPPER_STAIRS)
            randomTickBlocks.add(BlockID.CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.EXPOSED_CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.WEATHERED_CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.DOUBLE_CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.WEATHERED_DOUBLE_CUT_COPPER_SLAB)
            randomTickBlocks.add(BlockID.BUDDING_AMETHYST)
            randomTickBlocks.add(BlockID.POINTED_DRIPSTONE)
            randomTickBlocks.add(BlockID.CAVE_VINES)
            randomTickBlocks.add(BlockID.CAVE_VINES_BODY_WITH_BERRIES)
            randomTickBlocks.add(BlockID.CAVE_VINES_HEAD_WITH_BERRIES)
            randomTickBlocks.add(BlockID.TORCHFLOWER_CROP)
        }

        @JvmStatic
        fun canRandomTick(blockId: String): Boolean {
            return randomTickBlocks.contains(blockId)
        }

        fun setCanRandomTick(blockId: String, newValue: Boolean) {
            if (newValue) {
                randomTickBlocks.add(blockId)
            } else {
                randomTickBlocks.remove(blockId)
            }
        }

        @JvmStatic
        fun chunkHash(x: Int, z: Int): Long {
            return ((x.toLong()) shl 32) or (z.toLong() and 0xffffffffL)
        }

        @JvmStatic
        fun blockHash(x: Int, y: Int, z: Int, level: Level): Long {
            require(level.isYInRange(y)) { "Y coordinate y is out of range!" }
            return ((x.toLong() and 134217727L) shl 37) or ((level.ensureY(y) + 64).toLong() shl 28) or (z.toLong() and 0xFFFFFFFL)
        }

        fun localBlockHash(x: Double, y: Double, z: Double, level: Level): Int {
            val hi = ((x.toInt() and 15) + ((z.toInt() and 15) shl 4)).toByte()
            val lo = (level.ensureY(y.toInt()) + 64).toShort()
            return (hi.toInt() and 0xFF) shl 16 or lo.toInt()
        }

        fun localBlockHash(x: Int, y: Int, z: Int, layer: Int, level: Level): Int {
            val hi = ((x and 15) + ((z and 15) shl 4)).toByte()
            val lo = (level.ensureY(y) + 64).toShort()
            return ((layer and 127) shl 24) or ((hi.toInt() and 0xFF) shl 16) or lo.toInt()
        }

        fun getBlockXYZ(chunkHash: Long, blockHash: Int, level: Level): Vector3 {
            val hi = (blockHash ushr 16).toByte().toInt()
            val lo = blockHash.toShort().toInt()
            val y = level.ensureY(lo - 64)
            val x = (hi and 0xF) + (getHashX(chunkHash) shl 4)
            val z = ((hi shr 4) and 0xF) + (getHashZ(chunkHash) shl 4)
            return Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        }

        fun chunkBlockHash(x: Int, y: Int, z: Int): Int {
            return (x shl 13) or (z shl 9) or (y + 64) // 为适配384世界，y需要额外的1bit来存储
        }

        /**
         * 获取chunkX从chunk hash
         *
         *
         * Get chunkX from chunk hash
         *
         * @param hash the hash
         * @return the hash x
         */
        @JvmStatic
        fun getHashX(hash: Long): Int {
            return (hash shr 32).toInt()
        }

        /**
         * 获取chunkZ从chunk hash
         *
         *
         * Get chunkZ from chunk hash
         *
         * @param hash the hash
         * @return the hash x
         */
        @JvmStatic
        fun getHashZ(hash: Long): Int {
            return hash.toInt()
        }

        fun getBlockXYZ(hash: BlockVector3): Vector3 {
            return Vector3(hash.x.toDouble(), hash.y.toDouble(), hash.z.toDouble())
        }

        @JvmStatic
        fun generateChunkLoaderId(loader: ChunkLoader): Int {
            if (loader.loaderId == 0) {
                return chunkLoaderCounter++
            } else {
                throw IllegalStateException("ChunkLoader has a loader id already assigned: " + loader.loaderId)
            }
        }

        protected val VOID_BLOCK_COLOR: BlockColor = BlockColor.VOID_BLOCK_COLOR
        protected val WATER_BLOCK_COLOR: BlockColor = BlockColor.WATER_BLOCK_COLOR
    }
}
