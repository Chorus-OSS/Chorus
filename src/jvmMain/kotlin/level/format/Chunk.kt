package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntity.Companion.createBlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.biome.BiomeID
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.NumberTag
import org.chorus_oss.chorus.utils.Loggable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.StampedLock
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.math.min

class Chunk : IChunk {
    @Volatile
    override var x: Int = 0
        set(value) {
            field = value
            this.index = Level.chunkHash(x, z)
        }

    @Volatile
    override var z: Int = 0
        set(value) {
            field = value
            this.index = Level.chunkHash(x, z)
        }

    @Volatile
    override var index: Long = 0
        private set

    override var chunkState: ChunkState
        get() = atomicChunkState.get()
        set(value) = atomicChunkState.set(value)

    private var atomicChunkState = AtomicReference(ChunkState.NEW)

    val sections: Array<SubChunk?>

    override fun getSectionsSafe(): Array<SubChunk?> {
        val stamp = blockLock.readLock()
        try {
            return sections
        } finally {
            blockLock.unlockRead(stamp)
        }
    }

    override val heightMapArray: ShortArray //256 size Values start at 0 and are 0-384 for the Overworld range

    override val changes: Long
        get() = atomicChanges.get()

    private val atomicChanges = AtomicLong()

    override val entities: MutableMap<Long, Entity>
    val tiles: ConcurrentHashMap<Long, BlockEntity> //block entity id -> block entity
    protected val tileList: ConcurrentHashMap<Long, BlockEntity> //block entity position hash index -> block entity

    protected val blockLock: StampedLock
    protected val heightAndBiomeLock: StampedLock
    protected val lightLock: StampedLock
    override val provider: LevelProvider
    protected var isInit: Boolean = false
    protected var blockEntityNBT: List<CompoundTag>?
    protected var entityNBT: List<CompoundTag>?

    private constructor(
        chunkX: Int,
        chunkZ: Int,
        levelProvider: LevelProvider
    ) {
        this.x = chunkX
        this.z = chunkZ
        this.provider = levelProvider
        this.sections = arrayOfNulls(levelProvider.dimensionData.chunkSectionCount)
        this.heightMapArray = ShortArray(256)
        this.entities = ConcurrentHashMap()
        this.tiles = ConcurrentHashMap()
        this.tileList = ConcurrentHashMap()
        this.entityNBT = ArrayList()
        this.blockEntityNBT = ArrayList()
        this.blockLock = StampedLock()
        this.heightAndBiomeLock = StampedLock()
        this.lightLock = StampedLock()
    }

    private constructor(
        state: ChunkState,
        chunkX: Int,
        chunkZ: Int,
        levelProvider: LevelProvider,
        sections: Array<SubChunk?>,
        heightMap: ShortArray,
        entityNBT: List<CompoundTag>,
        blockEntityNBT: List<CompoundTag>
    ) {
        this.atomicChunkState = AtomicReference(state)
        this.x = chunkX
        this.z = chunkZ
        this.provider = levelProvider
        this.sections = sections
        this.heightMapArray = heightMap
        this.entities = ConcurrentHashMap()
        this.tiles = ConcurrentHashMap()
        this.tileList = ConcurrentHashMap()
        this.entityNBT = entityNBT
        this.blockEntityNBT = blockEntityNBT
        this.blockLock = StampedLock()
        this.heightAndBiomeLock = StampedLock()
        this.lightLock = StampedLock()
    }

    private fun getSectionInternal(fY: Int): SubChunk? {
        return sections[fY - dimensionData.minSectionY]
    }

    override fun getBlockState(x: Int, y: Int, z: Int, layer: Int): BlockState {
        var stamp = blockLock.tryOptimisticRead()
        try {
            while (true) {
                if (stamp == 0L) {
                    stamp = blockLock.readLock()
                    continue
                }
                val sectionInternal = getSectionInternal(y shr 4) ?: return BlockAir.STATE
                val result = sectionInternal.getBlockState(x, y and 0x0f, z, layer)
                if (!blockLock.validate(stamp)) {
                    stamp = blockLock.readLock()
                    continue
                }
                return result.also { stamp = blockLock.readLock() }
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) blockLock.unlockRead(stamp)
        }
    }

    override fun getAndSetBlockState(x: Int, y: Int, z: Int, blockstate: BlockState, layer: Int): BlockState {
        val stamp = blockLock.writeLock()
        try {
            setChanged()
            return getOrCreateSection(y shr 4)!!.getAndSetBlockState(x, y and 0x0f, z, blockstate, layer)
        } finally {
            blockLock.unlockWrite(stamp)
            removeInvalidTile(x, y, z)
        }
    }

    override fun setBlockState(x: Int, y: Int, z: Int, blockstate: BlockState, layer: Int) {
        val stamp = blockLock.writeLock()
        try {
            setChanged()
            getOrCreateSection(y shr 4)!!.setBlockState(x, y and 0x0f, z, blockstate, layer)
        } finally {
            blockLock.unlockWrite(stamp)
            removeInvalidTile(x, y, z)
        }
    }

    override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
        var stamp = lightLock.tryOptimisticRead()
        try {
            while (true) {
                if (stamp == 0L) {
                    stamp = lightLock.readLock()
                    continue
                }
                val sectionInternal = getSectionInternal(y shr 4) ?: return 0
                val result = sectionInternal.getBlockSkyLight(x, y and 0x0f, z).toInt()
                if (!lightLock.validate(stamp)) {
                    stamp = lightLock.readLock()
                    continue
                }
                return result.also { stamp = lightLock.readLock() }

            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) lightLock.unlockRead(stamp)
        }
    }

    override fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
        val stamp = lightLock.writeLock()
        try {
            setChanged()
            getOrCreateSection(y shr 4)!!.setBlockSkyLight(x, y and 0x0f, z, level.toByte())
        } finally {
            lightLock.unlockWrite(stamp)
        }
    }

    override fun getBlockLight(x: Int, y: Int, z: Int): Int {
        var stamp = lightLock.tryOptimisticRead()
        try {
            while (true) {
                if (stamp == 0L) {
                    stamp = lightLock.readLock()
                    continue
                }
                val sectionInternal = getSectionInternal(y shr 4) ?: return 0
                val result = sectionInternal.getBlockLight(x, y and 0x0f, z).toInt()
                if (!lightLock.validate(stamp)) {
                    stamp = lightLock.readLock()
                    continue
                }
                return result.also { stamp = lightLock.readLock() }
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) lightLock.unlockRead(stamp)
        }
    }

    override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
        val stamp = lightLock.writeLock()
        try {
            setChanged()
            getOrCreateSection(y shr 4)!!.setBlockLight(x, y and 0x0f, z, level.toByte())
        } finally {
            lightLock.unlockWrite(stamp)
        }
    }

    override fun getHeightMap(x: Int, z: Int): Int {
        var stamp = heightAndBiomeLock.tryOptimisticRead()
        try {
            while (true) {
                if (stamp == 0L) {
                    stamp = heightAndBiomeLock.readLock()
                    continue
                }
                val result = heightMapArray[(z shl 4) or x] + dimensionData.minHeight
                if (!heightAndBiomeLock.validate(stamp)) {
                    stamp = heightAndBiomeLock.readLock()
                    continue
                }
                return result.also { stamp = heightAndBiomeLock.readLock() }
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) heightAndBiomeLock.unlockRead(stamp)
        }
    }

    override fun setHeightMap(x: Int, z: Int, value: Int) {
        //基岩版3d-data保存heightMap是以0为索引保存的，所以这里需要减去世界最小值，详情查看
        //Bedrock Edition 3d-data saves the height map start from index of 0, so need to subtract the world minimum height here, see for details:
        //https://github.com/bedrock-dev/bedrock-level/blob/main/src/include/data_3d.h#L115
        val stamp = heightAndBiomeLock.writeLock()
        try {
            heightMapArray[(z shl 4) or x] = (value - dimensionData.minHeight).toShort()
        } finally {
            heightAndBiomeLock.unlockWrite(stamp)
        }
    }

    override fun recalculateHeightMap() {
        batchProcess { obj: UnsafeChunk -> obj.recalculateHeightMap() }
    }

    override fun recalculateHeightMapColumn(x: Int, z: Int): Int {
        val stamp1 = heightAndBiomeLock.writeLock()
        val stamp2 = blockLock.writeLock()
        try {
            val unsafeChunk = UnsafeChunk(this)
            val max = unsafeChunk.getHighestBlockAt(x, z)
            var y = max
            while (y >= dimensionData.minHeight) {
                val blockState = unsafeChunk.getBlockState(x, y, z)
                val block = Block.get(blockState)
                if (block.lightFilter > 1 || block.diffusesSkyLight()) {
                    break
                }
                --y
            }
            unsafeChunk.setHeightMap(x, z, y)
            return y
        } finally {
            heightAndBiomeLock.unlockWrite(stamp1)
            blockLock.unlockWrite(stamp2)
        }
    }

    override fun populateSkyLight() {
        batchProcess { unsafe: UnsafeChunk ->
            // basic light calculation
            for (z in 0..15) {
                for (x in 0..15) { // iterating over all columns in chunk
                    val top = unsafe.getHeightMap(x, z) // top-most block
                    var y = dimensionData.maxHeight
                    while (y > top) {
                        // all the blocks above & including the top-most block in a column are exposed to sun and
                        // thus have a skylight value of 15
                        unsafe.setBlockSkyLight(x, y, z, 15)
                        --y
                    }

                    var light = 15 // light value that will be applied starting with the next block
                    var nextDecrease = 0 // decrease that that will be applied starting with the next block

                    y = top
                    while (y >= dimensionData.minHeight) {
                        // going under the top-most block
                        light -= nextDecrease // this light value will be applied for this block. The following checks are all about the next blocks

                        if (light < 0) {
                            light = 0
                        }

                        unsafe.setBlockSkyLight(x, y, z, light)

                        if (light == 0) { // skipping block checks, because everything under a block that has a skylight value
                            // of 0 also has a skylight value of 0
                            --y
                            continue
                        }

                        // START of checks for the next block
                        val block = unsafe.getBlockState(x, y, z).toBlock()

                        if (!block.isTransparent) { // if we encounter an opaque block, all the blocks under it will
                            // have a skylight value of 0 (the block itself has a value of 15, if it's a top-most block)
                            light = 0
                        } else if (block.diffusesSkyLight()) {
                            nextDecrease += 1 // skylight value decreases by one for each block under a block
                            // that diffuses skylight. The block itself has a value of 15 (if it's a top-most block)
                        } else {
                            nextDecrease += block.lightFilter // blocks under a light filtering block will have a skylight value
                            // decreased by the lightFilter value of that block. The block itself
                            // has a value of 15 (if it's a top-most block)
                        }
                        --y
                    }
                }
            }
        }
    }

    override fun batchProcess(unsafeChunkConsumer: Consumer<UnsafeChunk>) {
        val stamp1 = blockLock.writeLock()
        val stamp2 = heightAndBiomeLock.writeLock()
        val stamp3 = lightLock.writeLock()
        try {
            unsafeChunkConsumer.accept(UnsafeChunk(this))
        } catch (e: Exception) {
            log.error("An error occurred while executing chunk batch operation", e)
        } finally {
            blockLock.unlockWrite(stamp1)
            heightAndBiomeLock.unlockWrite(stamp2)
            lightLock.unlockWrite(stamp3)
        }
    }

    override fun getBiomeId(x: Int, y: Int, z: Int): Int {
        var stamp = heightAndBiomeLock.tryOptimisticRead()
        try {
            while (true) {
                if (stamp == 0L) {
                    stamp = heightAndBiomeLock.readLock()
                    continue
                }
                val sectionInternal = getSectionInternal(y shr 4) ?: return BiomeID.PLAINS
                val result = sectionInternal.getBiomeId(x, y and 0x0f, z)
                if (!heightAndBiomeLock.validate(stamp)) {
                    stamp = heightAndBiomeLock.readLock()
                    continue
                }
                return result.also { stamp = heightAndBiomeLock.readLock() }
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) heightAndBiomeLock.unlockRead(stamp)
        }
    }

    override fun setBiomeId(x: Int, y: Int, z: Int, biomeId: Int) {
        val stamp = heightAndBiomeLock.writeLock()
        try {
            setChanged()
            getOrCreateSection(y shr 4)!!.setBiomeId(x, y and 0x0f, z, biomeId)
        } finally {
            heightAndBiomeLock.unlockWrite(stamp)
        }
    }

    override fun addEntity(entity: Entity) {
        entities[entity.getRuntimeID()] = entity
        if (entity !is Player && this.isInit) {
            this.setChanged()
        }
    }

    override fun removeEntity(entity: Entity) {
        entities.remove(entity.getRuntimeID())
        if (entity !is Player && this.isInit) {
            this.setChanged()
        }
    }

    override fun addBlockEntity(blockEntity: BlockEntity) {
        tiles[blockEntity.id] = blockEntity
        val index =
            ((blockEntity.position.floorZ and 0x0f) shl 16) or ((blockEntity.position.floorX and 0x0f) shl 12) or (ensureY(
                blockEntity.position.floorY
            ) + 64)
        val entity = tileList[index.toLong()]
        if (tileList.containsKey(index.toLong()) && entity != blockEntity) {
            tiles.remove(entity!!.id)
            entity.close()
        }
        tileList[index.toLong()] = blockEntity
        if (this.isInit) {
            this.setChanged()
        }
    }

    override fun removeBlockEntity(blockEntity: BlockEntity) {
        tiles.remove(blockEntity.id)
        val index =
            ((blockEntity.position.floorZ and 0x0f) shl 16) or ((blockEntity.position.floorX and 0x0f) shl 12) or (ensureY(
                blockEntity.position.floorY
            ) + 64)
        tileList.remove(index.toLong())
        if (this.isInit) {
            this.setChanged()
        }
    }

    override val blockEntities: MutableMap<Long, BlockEntity>
        get() = tiles

    override fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return tileList[(z.toLong() shl 16) or (x.toLong() shl 12) or (y + 64).toLong()]
    }

    override val isLoaded: Boolean
        get() = provider.isChunkLoaded(this.x, this.z)

    override fun unload(save: Boolean, safe: Boolean): Boolean {
        val provider = this.provider
        if (save && changes != 0L) {
            provider.saveChunk(this.x, this.z)
        }
        if (safe) {
            for (entity in entities.values) {
                if (entity is Player) {
                    return false
                }
            }
        }
        for (entity in ArrayList(entities.values)) {
            if (entity is Player) {
                continue
            }
            entity.close()
        }

        for (blockEntity in ArrayList(
            blockEntities.values
        )) {
            blockEntity.close()
        }
        return true
    }

    override fun initChunk() {
        if (!this.isInit) {
            var changed = false
            if (this.entityNBT != null) {
                for (nbt in entityNBT!!) {
                    if (!nbt.contains("identifier")) {
                        this.setChanged()
                        continue
                    }
                    val pos = nbt.getList("Pos")
                    if (((pos[0] as NumberTag<*>).data.toInt() shr 4) != this.x || (((pos[2] as NumberTag<*>).data.toInt() shr 4) != this.z)) {
                        changed = true
                        continue
                    }
                    val entity = createEntity(nbt.getString("identifier"), this, nbt)
                    if (entity != null) {
                        changed = true
                    }
                }
                this.entityNBT = null
            }

            if (this.blockEntityNBT != null) {
                for (nbt in blockEntityNBT!!) {
                    if (!nbt.contains("id")) {
                        changed = true
                        continue
                    }
                    if ((nbt.getInt("x") shr 4) != this.x || ((nbt.getInt("z") shr 4) != this.z)) {
                        changed = true
                        continue
                    }
                    val blockEntity = createBlockEntity(
                        nbt.getString("id"),
                        this.provider.level, nbt
                    )
                    if (blockEntity == null) {
                        changed = true
                    }
                }
                this.blockEntityNBT = null
            }

            if (changed) {
                this.setChanged()
            }

            this.isInit = true
        }
    }

    override fun hasChanged(): Boolean {
        return changes != 0L
    }

    override fun setChanged() {
        atomicChanges.incrementAndGet()
    }

    override fun setChanged(changed: Boolean) {
        if (changed) {
            setChanged()
        } else {
            atomicChanges.set(0)
        }
    }

    override fun getSectionBlockChanges(sectionY: Int): Long {
        return sections[sectionY]!!.blockChanges.get()
    }

    override fun isBlockChangeAllowed(chunkX: Int, chunkY: Int, chunkZ: Int): Boolean {
        //todo complete
        return true
    }

    override fun scanBlocks(
        min: BlockVector3,
        max: BlockVector3,
        condition: BiPredicate<BlockVector3?, BlockState?>
    ): Stream<Block?>? {
        val offsetX = x shl 4
        val offsetZ = z shl 4
        return IntStream.rangeClosed(0, dimensionData.chunkSectionCount - 1)
            .mapToObj { sectionY: Int -> sections[sectionY] }
            .filter { section: SubChunk? -> section != null && !section.isEmpty }.parallel()
            .map { section: SubChunk? ->
                section!!.scanBlocks(
                    provider,
                    offsetX,
                    offsetZ,
                    min,
                    max,
                    condition
                )
            }
            .flatMap { obj: List<Block?> -> obj.stream() }
    }

    /**
     * Gets or create section.
     *
     * @param sectionY the section y range -4 ~ 19
     * @return the or create section
     */
    protected fun getOrCreateSection(sectionY: Int): SubChunk? {
        val minSectionY = this.dimensionData.minSectionY
        val offsetY = sectionY - minSectionY
        for (i in 0..offsetY) {
            if (sections[i] == null) {
                sections[i] = SubChunk((i + minSectionY).toByte())
            }
        }
        return sections[offsetY]
    }

    private fun removeInvalidTile(x: Int, y: Int, z: Int) {
        val entity = getBlockEntity(x, y, z)
        if (entity != null) {
            try {
                if (!entity.closed && entity.isBlockEntityValid) {
                    return
                }
            } catch (e: Exception) {
                try {
                    log.warn(
                        "Block entity validation of {} at {}, {} {} {} failed, removing as invalid.",
                        entity.javaClass.name,
                        provider.level.name,
                        entity.position.x,
                        entity.position.y,
                        entity.position.z,
                        e
                    )
                } catch (e2: Exception) {
                    e.addSuppressed(e2)
                    log.warn("Block entity validation failed", e)
                }
            }
            entity.close()
        }
    }

    override fun reObfuscateChunk() {
        for (section in sections) {
            section?.setNeedReObfuscate()
        }
    }

    private fun ensureY(y: Int): Int {
        val minHeight = dimensionData.minHeight
        val maxHeight = dimensionData.maxHeight
        return max(min(y.toDouble(), maxHeight.toDouble()), minHeight.toDouble()).toInt()
    }


    override fun toString(): String {
        return "Chunk{" +
                "x=" + x +
                ", z=" + z +
                '}'
    }

    class Builder : IChunkBuilder {
        var state: ChunkState = ChunkState.NEW
        override var chunkZ: Int = 0
        override var chunkX: Int = 0
        private var levelProvider: LevelProvider? = null
        override var sections: Array<SubChunk?> = emptyArray()
        private var heightMap: ShortArray = ShortArray(256)
        var entities: List<CompoundTag> = listOf()
        private var blockEntities: List<CompoundTag> = listOf()

        override fun chunkX(chunkX: Int): Builder {
            this.chunkX = chunkX
            return this
        }

        override fun chunkZ(chunkZ: Int): Builder {
            this.chunkZ = chunkZ
            return this
        }

        override fun state(state: ChunkState): Builder {
            this.state = state
            return this
        }

        override fun levelProvider(levelProvider: LevelProvider): Builder {
            this.levelProvider = levelProvider
            this.sections = arrayOfNulls(levelProvider.dimensionData.chunkSectionCount)
            return this
        }

        override val dimensionData: DimensionData
            get() {
                return levelProvider!!.dimensionData
            }

        override fun sections(sections: Array<SubChunk?>): Builder {
            this.sections = sections
            return this
        }

        override fun heightMap(heightMap: ShortArray): Builder {
            this.heightMap = heightMap
            return this
        }

        override fun entities(entities: List<CompoundTag>): Builder {
            this.entities = entities
            return this
        }

        override fun blockEntities(blockEntities: List<CompoundTag>): Builder {
            this.blockEntities = blockEntities
            return this
        }

        override fun build(): Chunk {
            checkNotNull(levelProvider)
            return Chunk(
                state,
                chunkX,
                chunkZ,
                levelProvider!!,
                sections,
                heightMap,
                entities,
                blockEntities
            )
        }

        override fun emptyChunk(chunkX: Int, chunkZ: Int): Chunk {
            checkNotNull(levelProvider)
            return Chunk(chunkX, chunkZ, levelProvider!!)
        }
    }

    companion object : Loggable {
        fun builder(): Builder {
            return Builder()
        }
    }
}
