package org.chorus_oss.chorus.level.format.leveldb

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import org.chorus_oss.chorus.blockentity.BlockEntitySpawnable
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.*
import org.chorus_oss.chorus.level.format.LevelConfig.GeneratorConfig
import org.chorus_oss.chorus.level.format.leveldb.LevelDat.*
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.network.protocol.types.GameType
import org.chorus_oss.chorus.utils.*
import org.iq80.leveldb.CompressionType
import org.iq80.leveldb.Options
import java.io.*
import java.lang.ref.WeakReference
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class LevelDBProvider(override val level: Level, override val path: String) : LevelProvider {
    private val lastChunk: ThreadLocal<WeakReference<IChunk>?> = ThreadLocal()
    protected val chunks: ConcurrentHashMap<Long, IChunk> = ConcurrentHashMap()
    protected val storage: LevelDBStorage = CACHE.computeIfAbsent(path) { p: String ->
        try {
            return@computeIfAbsent LevelDBStorage(
                level.dimensionCount, p, Options()
                    .createIfMissing(true)
                    .compressionType(CompressionType.ZLIB_RAW)
                    .blockSize(64 * 1024)
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    val levelData: LevelDat = readLevelDat() ?: LevelDat().also {
        this.levelData = it
        saveLevelData()
    }

    fun loadChunk(index: Long, chunkX: Int, chunkZ: Int, create: Boolean): IChunk {
        var chunk: IChunk?
        try {
            chunk = storage.readChunk(chunkX, chunkZ, this)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        if (chunk == null) {
            if (create) {
                chunk = this.getEmptyChunk(chunkX, chunkZ)
                putChunk(index, chunk)
            }
        } else {
            putChunk(index, chunk)
        }
        return chunk
    }

    fun size(): Int {
        return chunks.size
    }

    override fun unloadChunks() {
        val iter = chunks.values.iterator()
        while (iter.hasNext()) {
            iter.next().unload(true, false)
            iter.remove()
        }
    }

    override val loadedChunks: MutableMap<Long, IChunk>
        get() = Collections.unmodifiableMap(chunks)

    override fun isChunkLoaded(x: Int, z: Int): Boolean {
        return isChunkLoaded(Level.chunkHash(x, z))
    }

    private fun putChunk(index: Long, chunk: IChunk) {
        chunks[index] = chunk
    }

    override fun isChunkLoaded(hash: Long): Boolean {
        return chunks.containsKey(hash)
    }

    override fun setChunk(chunkX: Int, chunkZ: Int, chunk: IChunk) {
        chunk.setPosition(chunkX, chunkZ)
        val index: Long = Level.chunkHash(chunkX, chunkZ)
        if (chunks.containsKey(index) && chunks[index] != chunk) {
            this.unloadChunk(chunkX, chunkZ, false)
        }
        lastChunk.remove() //remove cache
        putChunk(index, chunk)
    }

    override val dimensionData: DimensionData
        get() = level.dimensionData

    override fun requestChunkData(x: Int, z: Int): Pair<ByteArray, Int> {
        val chunk = this.getChunk(x, z, false)
        val data = AtomicReference<ByteArray>()
        val subChunkCountRef = AtomicReference<Int>()
        chunk.batchProcess { unsafeChunk: UnsafeChunk ->
            val byteBuf = ByteBufAllocator.DEFAULT.ioBuffer()
            try {
                val sections = unsafeChunk.sections
                var subChunkCount = unsafeChunk.dimensionData.chunkSectionCount
                while (subChunkCount-- != 0) {
                    if (sections[subChunkCount] != null) {
                        break
                    }
                }
                val total = subChunkCount + 1
                // write block
                if (level.isAntiXrayEnabled) {
                    for (i in 0..<total) {
                        if (sections[i] == null) {
                            sections[i] = SubChunk((i + dimensionData.minSectionY).toByte())
                        }
                        checkNotNull(sections[i]).writeObfuscatedToBuf(level, byteBuf)
                    }
                } else {
                    for (i in 0..<total) {
                        if (sections[i] == null) {
                            sections[i] = SubChunk((i + dimensionData.minSectionY).toByte())
                        }
                        checkNotNull(sections[i]).writeToBuf(byteBuf)
                    }
                }

                // Write biomes
                for (i in 0..<total) {
                    sections[i]!!.biomes.writeToNetwork(byteBuf) { it }
                }

                byteBuf.writeByte(0) // edu- border blocks

                // Block entities

                val tagList: MutableList<CompoundTag> = ArrayList()
                for (blockEntity in unsafeChunk.blockEntities.values) {
                    if (blockEntity is BlockEntitySpawnable) {
                        tagList.add(blockEntity.spawnCompound)
                        // Adding NBT to a chunk pack does not show some block entities, and you have to send block entity packets to the player
                        level.addChunkPacket(
                            blockEntity.position.chunkX,
                            blockEntity.position.chunkZ,
                            blockEntity.spawnPacket
                        )
                    }
                }
                try {
                    ByteBufOutputStream(byteBuf).use { stream ->
                        NBTIO.write(tagList, stream, ByteOrder.LITTLE_ENDIAN, true)
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }

                data.set(Utils.convertByteBuf2Array(byteBuf))
                subChunkCountRef.set(total)
            } finally {
                byteBuf.release()
            }
        }
        return Pair(data.get(), subChunkCountRef.get())
    }


    override val name: String
        get() = levelData.name

    override var rainTime: Int
        get() = levelData.rainTime
        set(rainTime) {
            levelData.rainTime = (rainTime)
        }

    override var thunderTime: Int
        get() = levelData.lightningTime
        set(thunderTime) {
            levelData.lightningTime = (thunderTime)
        }

    override var currentTick: Long
        get() = levelData.currentTick
        set(currentTick) {
            levelData.currentTick = (currentTick)
        }

    override var time: Long
        get() = levelData.time
        set(value) {
            levelData.time = value
        }

    override var seed: Long
        get() = levelData.randomSeed
        set(value) {
            levelData.randomSeed = (value)
        }

    override var spawn: Vector3
        get() = levelData.spawnPoint.asVector3().add(0.5, 0.0, 0.5)
        set(pos) {
            levelData.spawnPoint = (BlockVector3(pos.x.toInt(), pos.y.toInt(), pos.z.toInt()))
        }

    override val gamerules: GameRules
        get() = levelData.gameRules

    override fun setGameRules(rules: GameRules) {
        levelData.gameRules = (rules)
    }

    override fun saveChunks() {
        for (chunk in chunks.values) {
            if (chunk.changes != 0L) {
                chunk.setChanged(false)
                this.saveChunk(chunk.x, chunk.z)
            }
        }
    }

    override fun saveChunk(x: Int, z: Int) {
        val chunk = this.getChunk(x, z)
        if (chunk != null) {
            try {
                storage.writeChunk(chunk)
            } catch (e: Exception) {
                throw ChunkException("Error saving chunk ($x, $z)", e)
            }
        }
    }

    override fun saveChunk(x: Int, z: Int, chunk: IChunk) {
        chunk.x = x
        chunk.z = z
        try {
            storage.writeChunk(chunk)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun saveLevelData() {
        writeLevelDat(path, dimensionData, levelData)
    }

    override fun updateLevelName(name: String) {
        if (this.name != name) {
            levelData.name = name
        }
    }

    override fun loadChunk(x: Int, z: Int): Boolean {
        return this.loadChunk(x, z, false)
    }

    override fun loadChunk(x: Int, z: Int, create: Boolean): Boolean {
        val index: Long = Level.chunkHash(x, z)
        if (chunks.containsKey(index)) {
            return true
        }
        return loadChunk(index, x, z, create) != null
    }

    override fun unloadChunk(x: Int, z: Int): Boolean {
        return this.unloadChunk(x, z, true)
    }

    override fun unloadChunk(x: Int, z: Int, safe: Boolean): Boolean {
        val index: Long = Level.chunkHash(x, z)
        val chunk = chunks[index]
        if (chunk != null && chunk.unload(false, safe)) {
            lastChunk.remove()
            chunks.remove(index, chunk)
            return true
        }
        return false
    }

    override fun getChunk(x: Int, z: Int): IChunk {
        return this.getChunk(x, z, false)
    }

    protected val threadLastChunk: IChunk?
        get() {
            val ref = lastChunk.get() ?: return null
            return ref.get()
        }

    override fun getLoadedChunk(x: Int, z: Int): IChunk? {
        var tmp = threadLastChunk
        if (tmp != null && tmp.x == x && tmp.z == z) {
            return tmp
        }
        val index: Long = Level.chunkHash(x, z)
        lastChunk.set(WeakReference(chunks[index].also { tmp = it }))
        return tmp
    }

    override fun getLoadedChunk(hash: Long): IChunk? {
        var tmp = threadLastChunk
        if (tmp != null && tmp.index == hash) {
            return tmp
        }
        lastChunk.set(WeakReference(chunks[hash].also { tmp = it }))
        return tmp
    }

    override fun getChunk(x: Int, z: Int, create: Boolean): IChunk {
        val index: Long = Level.chunkHash(x, z)
        var tmp = getLoadedChunk(index)
        if (tmp == null) {
            tmp = this.loadChunk(index, x, z, create)
            lastChunk.set(WeakReference(tmp))
        }
        return tmp
    }

    override fun getEmptyChunk(x: Int, z: Int): IChunk {
        return Chunk.builder().levelProvider(this).emptyChunk(x, z)
    }

    override fun isChunkPopulated(x: Int, z: Int): Boolean {
        val chunk = this.getChunk(x, z)
        return chunk != null && chunk.chunkState.ordinal >= 2
    }

    override fun close() {
        storage.close()
    }

    override fun isChunkGenerated(x: Int, z: Int): Boolean {
        return true
    }

    @Synchronized
    @Throws(IOException::class)
    fun readLevelDat(): LevelDat? {
        val levelDat = Path.of(path).resolve("level.dat").toFile()
        if (!levelDat.exists()) return null
        try {
            FileInputStream(levelDat).use { input ->
                //The first 8 bytes are magic number
                input.skip(8)
                val stream = BufferedInputStream(ByteArrayInputStream(input.readAllBytes()))
                val d = NBTIO.read(stream, ByteOrder.LITTLE_ENDIAN)
                stream.close()
                val abilities = d.getCompound("abilities")
                val experiments = d.getCompound("experiments")
                val gameRules: GameRules = GameRules.default
                gameRules.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, d.getBoolean("bonusChestSpawned"))
                gameRules.setGameRule(GameRule.COMMAND_BLOCKS_ENABLED, d.getBoolean("commandblocksenabled"))
                gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, d.getBoolean("dodaylightcycle"))
                gameRules.setGameRule(GameRule.DO_ENTITY_DROPS, d.getBoolean("doentitydrops"))
                gameRules.setGameRule(GameRule.DO_FIRE_TICK, d.getBoolean("dofiretick"))
                gameRules.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, d.getBoolean("doimmediaterespawn"))
                gameRules.setGameRule(GameRule.DO_INSOMNIA, d.getBoolean("doinsomnia"))
                gameRules.setGameRule(GameRule.DO_LIMITED_CRAFTING, d.getBoolean("dolimitedcrafting"))
                gameRules.setGameRule(GameRule.DO_MOB_LOOT, d.getBoolean("domobloot"))
                gameRules.setGameRule(GameRule.DO_MOB_SPAWNING, d.getBoolean("domobspawning"))
                gameRules.setGameRule(GameRule.DO_TILE_DROPS, d.getBoolean("dotiledrops"))
                gameRules.setGameRule(GameRule.DO_WEATHER_CYCLE, d.getBoolean("doweathercycle"))
                gameRules.setGameRule(GameRule.DROWNING_DAMAGE, d.getBoolean("drowningdamage"))
                gameRules.setGameRule(GameRule.FALL_DAMAGE, d.getBoolean("falldamage"))
                gameRules.setGameRule(GameRule.FIRE_DAMAGE, d.getBoolean("firedamage"))
                gameRules.setGameRule(GameRule.FREEZE_DAMAGE, d.getBoolean("freezedamage"))
                gameRules.setGameRule(GameRule.FUNCTION_COMMAND_LIMIT, d.getInt("functioncommandlimit"))
                gameRules.setGameRule(GameRule.KEEP_INVENTORY, d.getBoolean("keepinventory"))
                gameRules.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, d.getInt("maxcommandchainlength"))
                gameRules.setGameRule(GameRule.MOB_GRIEFING, d.getBoolean("mobgriefing"))
                gameRules.setGameRule(GameRule.NATURAL_REGENERATION, d.getBoolean("naturalregeneration"))
                gameRules.setGameRule(GameRule.PVP, d.getBoolean("pvp"))
                gameRules.setGameRule(GameRule.RESPAWN_BLOCKS_EXPLODE, d.getBoolean("respawnblocksexplode"))
                gameRules.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, d.getBoolean("sendcommandfeedback"))
                gameRules.setGameRule(GameRule.SHOW_BORDER_EFFECT, d.getBoolean("showbordereffect"))
                gameRules.setGameRule(GameRule.SHOW_COORDINATES, d.getBoolean("showcoordinates"))
                gameRules.setGameRule(GameRule.SHOW_DEATH_MESSAGES, d.getBoolean("showdeathmessages"))
                gameRules.setGameRule(GameRule.SHOW_TAGS, d.getBoolean("showtags"))
                gameRules.setGameRule(GameRule.SPAWN_RADIUS, d.getInt("spawnradius"))
                gameRules.setGameRule(GameRule.TNT_EXPLODES, d.getBoolean("tntexplodes"))
                val levelDatBuilder = LevelDat()
                levelDatBuilder.biomeOverride = (d.getString("BiomeOverride"))
                levelDatBuilder.centerMapsToOrigin = (d.getBoolean("CenterMapsToOrigin"))
                levelDatBuilder.confirmedPlatformLockedContent = (d.getBoolean("ConfirmedPlatformLockedContent"))
                levelDatBuilder.difficulty = (d.getInt("Difficulty"))
                levelDatBuilder.flatWorldLayers = (d.getString("FlatWorldLayers"))
                levelDatBuilder.forceGameType = (d.getBoolean("ForceGameType"))
                levelDatBuilder.gameType = (GameType.from(d.getInt("GameType")))
                levelDatBuilder.generator = (d.getInt("Generator"))
                levelDatBuilder.inventoryVersion = (d.getString("InventoryVersion"))
                levelDatBuilder.LANBroadcast = (d.getBoolean("LANBroadcast"))
                levelDatBuilder.LANBroadcastIntent = (d.getBoolean("LANBroadcastIntent"))
                levelDatBuilder.lastPlayed = (d.getLong("LastPlayed"))
                levelDatBuilder.name = (d.getString("LevelName"))
                levelDatBuilder.limitedWorldOriginPoint = (
                        BlockVector3(
                            d.getInt("LimitedWorldOriginX"),
                            d.getInt("LimitedWorldOriginY"),
                            d.getInt("LimitedWorldOriginZ")
                        )
                        )
                levelDatBuilder.minimumCompatibleClientVersion = (
                        SemVersion.from(
                            d.getList(
                                "MinimumCompatibleClientVersion",
                                IntTag::class.java
                            )
                        )
                        )
                levelDatBuilder.multiplayerGame = (d.getBoolean("MultiplayerGame"))
                levelDatBuilder.multiplayerGameIntent = (d.getBoolean("MultiplayerGameIntent"))
                levelDatBuilder.netherScale = (d.getInt("NetherScale"))
                levelDatBuilder.networkVersion = (d.getInt("NetworkVersion"))
                levelDatBuilder.platform = (d.getInt("Platform"))
                levelDatBuilder.platformBroadcastIntent = (d.getInt("PlatformBroadcastIntent"))
                levelDatBuilder.randomSeed = (d.getLong("RandomSeed"))
                levelDatBuilder.spawnV1Villagers = (d.getBoolean("SpawnV1Villagers"))
                levelDatBuilder.spawnPoint = (BlockVector3(d.getInt("SpawnX"), d.getInt("SpawnY"), d.getInt("SpawnZ")))
                levelDatBuilder.storageVersion = (d.getInt("StorageVersion"))
                levelDatBuilder.time = (d.getLong("Time"))
                levelDatBuilder.worldVersion = (d.getInt("WorldVersion"))
                levelDatBuilder.XBLBroadcastIntent = (d.getInt("XBLBroadcastIntent"))
                levelDatBuilder.gameRules = (gameRules)
                levelDatBuilder.abilities = (
                        run {
                            val levelAbilities = Abilities()
                            levelAbilities.attackMobs = (abilities.getBoolean("attackmobs"))
                            levelAbilities.attackPlayers = (abilities.getBoolean("attackplayers"))
                            levelAbilities.build = (abilities.getBoolean("build"))
                            levelAbilities.doorsAndSwitches = (abilities.getBoolean("doorsandswitches"))
                            levelAbilities.flySpeed = (abilities.getFloat("flySpeed"))
                            levelAbilities.flying = (abilities.getBoolean("flying"))
                            levelAbilities.instaBuild = (abilities.getBoolean("instabuild"))
                            levelAbilities.invulnerable = (abilities.getBoolean("invulnerable"))
                            levelAbilities.lightning = (abilities.getBoolean("lightning"))
                            levelAbilities.mayFly = (abilities.getBoolean("mayfly"))
                            levelAbilities.mine = (abilities.getBoolean("mine"))
                            levelAbilities.op = (abilities.getBoolean("op"))
                            levelAbilities.openContainers = (abilities.getBoolean("opencontainers"))
                            levelAbilities.teleport = (abilities.getBoolean("teleport"))
                            levelAbilities.walkSpeed = (abilities.getFloat("walkSpeed"))
                            return@run levelAbilities
                        }

                        )
                levelDatBuilder.baseGameVersion = (d.getString("baseGameVersion"))
                levelDatBuilder.bonusChestEnabled = (d.getBoolean("bonusChestEnabled"))
                levelDatBuilder.bonusChestSpawned = (d.getBoolean("bonusChestSpawned"))
                levelDatBuilder.cheatsEnabled = (d.getBoolean("cheatsEnabled"))
                levelDatBuilder.commandsEnabled = (d.getBoolean("commandsEnabled"))
                levelDatBuilder.currentTick = (d.getLong("currentTick"))
                levelDatBuilder.daylightCycle = (d.getInt("daylightCycle"))
                levelDatBuilder.editorWorldType = (d.getInt("editorWorldType"))
                levelDatBuilder.eduOffer = (d.getInt("eduOffer"))
                levelDatBuilder.educationFeaturesEnabled = (d.getBoolean("educationFeaturesEnabled"))
                levelDatBuilder.experiments = (
                        run {
                            val levelExperiments = Experiments()
                            levelExperiments.cameras = (experiments.getBoolean("cameras"))
                            levelExperiments.dataDrivenBiomes = (experiments.getBoolean("data_driven_biomes"))
                            levelExperiments.dataDrivenItems = (experiments.getBoolean("data_driven_items"))
                            levelExperiments.experimentalMolangFeatures =
                                (experiments.getBoolean("experimental_molang_features"))
                            levelExperiments.experimentsEverUsed = (experiments.getBoolean("experiments_ever_used"))
                            levelExperiments.savedWithToggledExperiments =
                                (experiments.getBoolean("saved_with_toggled_experiments"))
                            levelExperiments.upcomingCreatorFeatures =
                                (experiments.getBoolean("upcoming_creator_features"))
                            levelExperiments.villagerTradesRebalance =
                                (experiments.getBoolean("villager_trades_rebalance"))
                            return@run levelExperiments
                        }
                        )
                levelDatBuilder.hasBeenLoadedInCreative = (d.getBoolean("hasBeenLoadedInCreative"))
                levelDatBuilder.hasLockedBehaviorPack = (d.getBoolean("hasLockedBehaviorPack"))
                levelDatBuilder.hasLockedResourcePack = (d.getBoolean("hasLockedResourcePack"))
                levelDatBuilder.immutableWorld = (d.getBoolean("immutableWorld"))
                levelDatBuilder.isCreatedInEditor = (d.getBoolean("isCreatedInEditor"))
                levelDatBuilder.isExportedFromEditor = (d.getBoolean("isExportedFromEditor"))
                levelDatBuilder.isFromLockedTemplate = (d.getBoolean("isFromLockedTemplate"))
                levelDatBuilder.isFromWorldTemplate = (d.getBoolean("isFromWorldTemplate"))
                levelDatBuilder.isRandomSeedAllowed = (d.getBoolean("isRandomSeedAllowed"))
                levelDatBuilder.isSingleUseWorld = (d.getBoolean("isSingleUseWorld"))
                levelDatBuilder.isWorldTemplateOptionLocked = (d.getBoolean("isWorldTemplateOptionLocked"))
                levelDatBuilder.lastOpenedWithVersion =
                    (SemVersion.from(d.getList("lastOpenedWithVersion", IntTag::class.java)))
                levelDatBuilder.lightningLevel = (d.getFloat("lightningLevel"))
                levelDatBuilder.lightningTime = (d.getInt("lightningTime"))
                levelDatBuilder.limitedWorldDepth = (d.getInt("limitedWorldDepth"))
                levelDatBuilder.limitedWorldWidth = (d.getInt("limitedWorldWidth"))
                levelDatBuilder.permissionsLevel = (d.getInt("permissionsLevel"))
                levelDatBuilder.playerPermissionsLevel = (d.getInt("playerPermissionsLevel"))
                levelDatBuilder.playersSleepingPercentage = (d.getInt("playerssleepingpercentage"))
                levelDatBuilder.prid = (d.getString("prid"))
                levelDatBuilder.rainLevel = (d.getFloat("rainLevel"))
                levelDatBuilder.rainTime = (d.getInt("rainTime"))
                levelDatBuilder.randomTickSpeed = (d.getInt("randomtickspeed"))
                levelDatBuilder.recipesUnlock = (d.getBoolean("recipesunlock"))
                levelDatBuilder.requiresCopiedPackRemovalCheck = (d.getBoolean("requiresCopiedPackRemovalCheck"))
                levelDatBuilder.serverChunkTickRange = (d.getInt("serverChunkTickRange"))
                levelDatBuilder.spawnMobs = (d.getBoolean("spawnMobs"))
                levelDatBuilder.startWithMapEnabled = (d.getBoolean("startWithMapEnabled"))
                levelDatBuilder.texturePacksRequired = (d.getBoolean("texturePacksRequired"))
                levelDatBuilder.useMsaGamertagsOnly = (d.getBoolean("useMsaGamertagsOnly"))
                levelDatBuilder.worldStartCount = (d.getLong("worldStartCount"))
                levelDatBuilder.worldPolicies = (WorldPolicies())
                return levelDatBuilder
            }
        } catch (e: FileNotFoundException) {
            log.error("The level.dat file does not exist!")
        }
        throw RuntimeException("level.dat is null!")
    }

    companion object : Loggable {
        val CACHE: HashMap<String, LevelDBStorage> = HashMap()
        private val levelDatMagic = byteArrayOf(10, 0, 0, 0, 68, 11, 0, 0)

        @Throws(IOException::class)
        fun generate(path: String, name: String, generatorConfig: GeneratorConfig) {
            val dataDir = File("$path/db")
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                throw IOException("Could not create the directory $dataDir")
            }
            val levelData = LevelDat()
            levelData.randomSeed = (generatorConfig.seed)
            levelData.name = (name)
            levelData.lastPlayed = (System.currentTimeMillis() / 1000)
            writeLevelDat(path, generatorConfig.dimensionData, levelData)
        }

        fun isValid(path: String?): Boolean {
            val isValid = (File(path, "level.dat").exists()) && File(path, "db").isDirectory
            if (isValid) {
                for (file in requireNotNull(File(path, "db").listFiles())) {
                    if (file.name.endsWith(".ldb")) {
                        return true
                    }
                }
            }
            return false
        }

        fun writeLevelDat(pathName: String, dimensionData: DimensionData, levelDat: LevelDat) {
            val path = Path.of(pathName)
            var levelDatName = "level.dat"
            if (dimensionData.dimensionId != 0) {
                levelDatName = "level_Dim${dimensionData.dimensionId}.dat"
            }
            val levelDatNow = path.resolve(levelDatName).toFile()
            try {
                FileOutputStream(levelDatNow).use { output ->
                    if (levelDatNow.exists()) {
                        Files.copy(
                            path.resolve(levelDatName),
                            path.resolve(levelDatName + "_old"),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                    } else {
                        levelDatNow.createNewFile()
                    }
                    output.write(levelDatMagic) // magic number
                    NBTIO.write(createWorldDataNBT(levelDat), output, ByteOrder.LITTLE_ENDIAN)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        private fun createWorldDataNBT(worldData: LevelDat): CompoundTag {
            val levelDat = CompoundTag()

            levelDat.putString("BiomeOverride", worldData.biomeOverride)
            levelDat.putBoolean("CenterMapsToOrigin", worldData.centerMapsToOrigin)
            levelDat.putBoolean("ConfirmedPlatformLockedContent", worldData.confirmedPlatformLockedContent)
            levelDat.putInt("Difficulty", worldData.difficulty)
            levelDat.putString("FlatWorldLayers", worldData.flatWorldLayers)
            levelDat.putBoolean("ForceGameType", worldData.forceGameType)
            levelDat.putInt("GameType", worldData.gameType.ordinal)
            levelDat.putInt("Generator", worldData.generator)
            levelDat.putString("InventoryVersion", worldData.inventoryVersion)
            levelDat.putBoolean("LANBroadcast", worldData.LANBroadcast)
            levelDat.putBoolean("LANBroadcastIntent", worldData.LANBroadcastIntent)
            levelDat.putLong("LastPlayed", worldData.lastPlayed)
            levelDat.putString("LevelName", worldData.name)
            levelDat.putInt("LimitedWorldOriginX", worldData.limitedWorldOriginPoint.x)
            levelDat.putInt("LimitedWorldOriginY", worldData.limitedWorldOriginPoint.y)
            levelDat.putInt("LimitedWorldOriginZ", worldData.limitedWorldOriginPoint.z)
            levelDat.putList("MinimumCompatibleClientVersion", worldData.minimumCompatibleClientVersion.toTag())
            levelDat.putList("lastOpenedWithVersion", worldData.lastOpenedWithVersion.toTag())
            levelDat.putBoolean("MultiplayerGame", worldData.multiplayerGame)
            levelDat.putBoolean("MultiplayerGameIntent", worldData.multiplayerGameIntent)
            levelDat.putInt("NetherScale", worldData.netherScale)
            levelDat.putInt("NetworkVersion", worldData.networkVersion)
            levelDat.putInt("Platform", worldData.platform)
            levelDat.putInt("PlatformBroadcastIntent", worldData.platformBroadcastIntent)
            levelDat.putLong("RandomSeed", worldData.randomSeed)
            levelDat.putBoolean("SpawnV1Villagers", worldData.spawnV1Villagers)
            levelDat.putInt("SpawnX", worldData.spawnPoint.x)
            levelDat.putInt("SpawnY", worldData.spawnPoint.y)
            levelDat.putInt("SpawnZ", worldData.spawnPoint.z)
            levelDat.putInt("StorageVersion", worldData.storageVersion)
            levelDat.putLong("Time", worldData.time)
            levelDat.putInt("WorldVersion", worldData.worldVersion)
            levelDat.putInt("XBLBroadcastIntent", worldData.XBLBroadcastIntent)
            val abilities = CompoundTag()
                .putBoolean("attackmobs", worldData.abilities.attackMobs)
                .putBoolean("attackplayers", worldData.abilities.attackPlayers)
                .putBoolean("build", worldData.abilities.build)
                .putBoolean("doorsandswitches", worldData.abilities.doorsAndSwitches)
                .putBoolean("flying", worldData.abilities.flying)
                .putBoolean("instabuild", worldData.abilities.instaBuild)
                .putBoolean("invulnerable", worldData.abilities.invulnerable)
                .putBoolean("lightning", worldData.abilities.lightning)
                .putBoolean("mayfly", worldData.abilities.mayFly)
                .putBoolean("mine", worldData.abilities.mine)
                .putBoolean("op", worldData.abilities.op)
                .putBoolean("opencontainers", worldData.abilities.openContainers)
                .putBoolean("teleport", worldData.abilities.teleport)
                .putFloat("flySpeed", worldData.abilities.flySpeed)
                .putFloat("walkSpeed", worldData.abilities.walkSpeed)
            val experiments = CompoundTag()
                .putBoolean("cameras", worldData.experiments.cameras)
                .putBoolean("data_driven_biomes", worldData.experiments.dataDrivenBiomes)
                .putBoolean("data_driven_items", worldData.experiments.dataDrivenItems)
                .putBoolean("experimental_molang_features", worldData.experiments.experimentalMolangFeatures)
                .putBoolean("experiments_ever_used", worldData.experiments.experimentsEverUsed)
                .putBoolean("gametest", worldData.experiments.gametest)
                .putBoolean("saved_with_toggled_experiments", worldData.experiments.savedWithToggledExperiments)
                .putBoolean("upcoming_creator_features", worldData.experiments.upcomingCreatorFeatures)
                .putBoolean("villager_trades_rebalance", worldData.experiments.villagerTradesRebalance)
            levelDat.put("abilities", abilities)
            levelDat.put("experiments", experiments)

            levelDat.putBoolean("bonusChestEnabled", worldData.bonusChestEnabled)
            levelDat.putBoolean("bonusChestSpawned", worldData.bonusChestSpawned)
            levelDat.putBoolean("cheatsEnabled", worldData.cheatsEnabled)
            levelDat.putBoolean("commandsEnabled", worldData.commandsEnabled)
            levelDat.putLong("currentTick", worldData.currentTick)
            levelDat.putInt("daylightCycle", worldData.daylightCycle)
            levelDat.putInt("editorWorldType", worldData.editorWorldType)
            levelDat.putInt("eduOffer", worldData.eduOffer)
            levelDat.putBoolean("educationFeaturesEnabled", worldData.educationFeaturesEnabled)

            levelDat.put(
                "commandblockoutput",
                worldData.gameRules.getGameRules()[GameRule.COMMAND_BLOCK_OUTPUT]!!.tag
            )
            levelDat.put(
                "commandblocksenabled",
                worldData.gameRules.getGameRules()[GameRule.COMMAND_BLOCKS_ENABLED]!!.tag
            )
            levelDat.put("dodaylightcycle", worldData.gameRules.getGameRules()[GameRule.DO_DAYLIGHT_CYCLE]!!.tag)
            levelDat.put("doentitydrops", worldData.gameRules.getGameRules()[GameRule.DO_ENTITY_DROPS]!!.tag)
            levelDat.put("dofiretick", worldData.gameRules.getGameRules()[GameRule.DO_FIRE_TICK]!!.tag)
            levelDat.put(
                "doimmediaterespawn",
                worldData.gameRules.getGameRules()[GameRule.DO_IMMEDIATE_RESPAWN]!!.tag
            )
            levelDat.put("doinsomnia", worldData.gameRules.getGameRules()[GameRule.DO_INSOMNIA]!!.tag)
            levelDat.put("dolimitedcrafting", worldData.gameRules.getGameRules()[GameRule.DO_LIMITED_CRAFTING]!!.tag)
            levelDat.put("domobloot", worldData.gameRules.getGameRules()[GameRule.DO_MOB_LOOT]!!.tag)
            levelDat.put("domobspawning", worldData.gameRules.getGameRules()[GameRule.DO_MOB_SPAWNING]!!.tag)
            levelDat.put("dotiledrops", worldData.gameRules.getGameRules()[GameRule.DO_TILE_DROPS]!!.tag)
            levelDat.put("doweathercycle", worldData.gameRules.getGameRules()[GameRule.DO_WEATHER_CYCLE]!!.tag)
            levelDat.put("drowningdamage", worldData.gameRules.getGameRules()[GameRule.DROWNING_DAMAGE]!!.tag)
            levelDat.put("falldamage", worldData.gameRules.getGameRules()[GameRule.FALL_DAMAGE]!!.tag)
            levelDat.put("firedamage", worldData.gameRules.getGameRules()[GameRule.FIRE_DAMAGE]!!.tag)
            levelDat.put("freezedamage", worldData.gameRules.getGameRules()[GameRule.FREEZE_DAMAGE]!!.tag)
            levelDat.put(
                "functioncommandlimit",
                worldData.gameRules.getGameRules()[GameRule.FUNCTION_COMMAND_LIMIT]!!.tag
            )
            levelDat.put("keepinventory", worldData.gameRules.getGameRules()[GameRule.KEEP_INVENTORY]!!.tag)
            levelDat.put(
                "maxcommandchainlength",
                worldData.gameRules.getGameRules()[GameRule.MAX_COMMAND_CHAIN_LENGTH]!!.tag
            )
            levelDat.put("mobgriefing", worldData.gameRules.getGameRules()[GameRule.MOB_GRIEFING]!!.tag)
            levelDat.put(
                "naturalregeneration",
                worldData.gameRules.getGameRules()[GameRule.NATURAL_REGENERATION]!!.tag
            )
            levelDat.put("pvp", worldData.gameRules.getGameRules()[GameRule.PVP]!!.tag)
            levelDat.put(
                "respawnblocksexplode",
                worldData.gameRules.getGameRules()[GameRule.RESPAWN_BLOCKS_EXPLODE]!!.tag
            )
            levelDat.put(
                "sendcommandfeedback",
                worldData.gameRules.getGameRules()[GameRule.SEND_COMMAND_FEEDBACK]!!.tag
            )
            levelDat.put("showbordereffect", worldData.gameRules.getGameRules()[GameRule.SHOW_BORDER_EFFECT]!!.tag)
            levelDat.put("showcoordinates", worldData.gameRules.getGameRules()[GameRule.SHOW_COORDINATES]!!.tag)
            levelDat.put("showdeathmessages", worldData.gameRules.getGameRules()[GameRule.SHOW_DEATH_MESSAGES]!!.tag)
            levelDat.put("showtags", worldData.gameRules.getGameRules()[GameRule.SHOW_TAGS]!!.tag)
            levelDat.put("spawnradius", worldData.gameRules.getGameRules()[GameRule.SPAWN_RADIUS]!!.tag)
            levelDat.put("tntexplodes", worldData.gameRules.getGameRules()[GameRule.TNT_EXPLODES]!!.tag)

            return levelDat
        }
    }
}
