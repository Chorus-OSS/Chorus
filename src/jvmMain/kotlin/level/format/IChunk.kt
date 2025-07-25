package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.math.BlockVector3
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Range
import java.io.IOException
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.stream.Stream

interface IChunk {
    /**
     * Is section empty.
     *
     * @param fY range -4 ~ 19 for Overworld
     * @return the boolean
     */
    fun isSectionEmpty(fY: Int): Boolean

    /**
     * Gets section.
     *
     * @param fY range -4 ~ 19 for Overworld
     * @return the section
     */
    fun getSection(fY: Int): SubChunk?

    /**
     * Sets section.
     *
     * @param fY      range -4 ~ 19 for Overworld
     * @param section the section
     */
    fun setSection(fY: Int, section: SubChunk?)

    fun getSectionsSafe(): Array<SubChunk?>

    var x: Int

    var z: Int

    fun setPosition(x: Int, z: Int) {
        this.x = x
        this.z = z
    }

    val index: Long

    val provider: LevelProvider

    val dimensionData: DimensionData
        get() = provider.dimensionData

    fun getBlockState(x: Int, y: Int, z: Int): BlockState? {
        return getBlockState(x, y, z, 0)
    }

    /**
     * Gets block.
     *
     * @param x     the x
     * @param y     the chunk y range -64 ~ 319 for OVERWORLD
     * @param z     the z
     * @param layer the layer
     * @return the block
     */
    fun getBlockState(x: Int, y: Int, z: Int, layer: Int): BlockState

    fun getAndSetBlockState(x: Int, y: Int, z: Int, blockstate: BlockState, layer: Int): BlockState?

    fun getAndSetBlockState(x: Int, y: Int, z: Int, blockstate: BlockState): BlockState? {
        return getAndSetBlockState(x, y, z, blockstate, 0)
    }

    fun setBlockState(x: Int, y: Int, z: Int, blockstate: BlockState, layer: Int)

    fun setBlockState(x: Int, y: Int, z: Int, blockstate: BlockState) {
        setBlockState(x, y, z, blockstate, 0)
    }

    /**
     * @param x the x 0~15
     * @param y the y
     * @param z the z 0~15
     * @return The block skylight at this location
     */
    fun getBlockSkyLight(x: Int, y: Int, z: Int): Int

    fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int)

    /**
     * @param x the x 0~15
     * @param y the y
     * @param z the z 0~15
     * @return The block light at this location
     */
    fun getBlockLight(x: Int, y: Int, z: Int): Int

    /**
     * Sets block light.
     *
     * @param x     the x 0~15
     * @param y     the y
     * @param z     the z 0~15
     * @param level the level 0~15
     */
    fun setBlockLight(x: Int, y: Int, z: Int, level: Int)

    /**
     * Get a heightmap in this section coordinates, which is the highest block height
     *
     * @param x the x 0~15
     * @param z the z 0~15
     * @return the height map
     */
    fun getHeightMap(x: Int, z: Int): Int

    /**
     * Sets height map for this coordinate,which is the highest block height
     *
     * @param x     the x 0~15
     * @param z     the z 0~15
     * @param value the value
     */
    fun setHeightMap(x: Int, z: Int, value: Int)

    /**
     * Recalculate height map for this chunk.
     */
    fun recalculateHeightMap()

    /**
     * Recalculate a column height map of chunk
     */
    fun recalculateHeightMapColumn(x: @Range(from = 0, to = 15) Int, z: @Range(from = 0, to = 15) Int): Int

    fun populateSkyLight()

    /**
     * 获取子区块中某个特定位置的生物群系id
     *
     * @param x 0~15
     * @param z 0~15
     * @return 特定位置的生物群系id
     */
    fun getBiomeId(x: Int, y: Int, z: Int): Int

    fun setBiomeId(x: Int, y: Int, z: Int, biomeId: Int)

    var chunkState: ChunkState

    fun addEntity(entity: Entity)

    fun removeEntity(entity: Entity)

    fun addBlockEntity(blockEntity: BlockEntity)

    fun removeBlockEntity(blockEntity: BlockEntity)

    val entities: MutableMap<Long, Entity>

    val blockEntities: MutableMap<Long, BlockEntity>

    fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity?

    @ApiStatus.Experimental
    fun batchProcess(unsafeChunkConsumer: Consumer<UnsafeChunk>)

    val isLoaded: Boolean

    @Throws(IOException::class)
    fun load(): Boolean

    @Throws(IOException::class)
    fun load(generate: Boolean): Boolean

    fun unload(): Boolean

    fun unload(save: Boolean): Boolean

    fun unload(save: Boolean, safe: Boolean): Boolean

    /**
     * Init chunk.Load block entity and entity NBT
     */
    fun initChunk()

    val heightMapArray: ShortArray

    fun hasChanged(): Boolean

    fun setChanged()

    fun setChanged(changed: Boolean)

    val changes: Long

    fun getSectionBlockChanges(sectionY: Int): Long

    /**
     * Used to handle with deny and allow blocks
     *
     * @return the boolean
     */
    fun isBlockChangeAllowed(chunkX: Int, chunkY: Int, chunkZ: Int): Boolean

    fun scanBlocks(
        min: BlockVector3,
        max: BlockVector3,
        condition: BiPredicate<BlockVector3?, BlockState?>
    ): Stream<Block?>?

    fun reObfuscateChunk() {
    }

    val isGenerated: Boolean
        get() = chunkState.ordinal >= ChunkState.GENERATED.ordinal

    val isPopulated: Boolean
        get() = chunkState.ordinal >= ChunkState.POPULATED.ordinal

    val isFinished: Boolean
        get() = chunkState.ordinal == ChunkState.FINISHED.ordinal

    fun setGenerated() {
        chunkState = ChunkState.GENERATED
    }

    fun setPopulated() {
        chunkState = ChunkState.POPULATED
    }

    companion object {
        /**
         * Get Palette index
         *
         * @param x the x
         * @param y the y
         * @param z the z
         * @return the int
         */
        fun index(x: Int, y: Int, z: Int): Int {
            // Chunk Order; Bedrock: XZY, Java: YZX
            return (x shl 8) + (z shl 4) + y
        }

        const val VERSION: Int = 40
    }
}
