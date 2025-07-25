package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.DimensionData
import org.jetbrains.annotations.ApiStatus

class UnsafeChunk(private val chunk: Chunk) {
    @get:ApiStatus.Internal
    val sections: Array<SubChunk?>
        get() = chunk.sections

    val dimensionData: DimensionData
        get() = chunk.dimensionData

    val blockEntities: Map<Long, BlockEntity>
        get() = chunk.tiles

    private fun setChanged() {
        chunk.setChanged()
    }

    /**
     * Gets or create section.
     *
     * @param sectionY the section y range -4 ~ 19
     * @return the or create section
     */
    private fun getOrCreateSection(sectionY: Int): SubChunk? {
        val minSectionY = dimensionData.minSectionY
        val offsetY = sectionY - minSectionY
        if (offsetY < 0) return null
        for (i in 0..offsetY) {
            if (chunk.sections[i] == null) {
                chunk.sections[i] = SubChunk((i + minSectionY).toByte())
            }
        }
        return chunk.sections[offsetY]
    }

    fun getSection(fY: Int): SubChunk? {
        return chunk.sections[fY - dimensionData.minSectionY]
    }

    fun getBlockState(x: Int, y: Int, z: Int): BlockState {
        val section = getSection(y shr 4) ?: return BlockAir.STATE
        return section.getBlockState(x, y and 0x0f, z, 0)
    }

    fun getBlockState(x: Int, y: Int, z: Int, layer: Int): BlockState {
        val section = getSection(y shr 4) ?: return BlockAir.STATE
        return section.getBlockState(x, y and 0x0f, z, layer)
    }

    fun setBlockState(x: Int, y: Int, z: Int, blockstate: BlockState, layer: Int) {
        getOrCreateSection(y shr 4)!!.setBlockState(x, y and 0x0f, z, blockstate, layer)
    }

    fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
        val section = getOrCreateSection(y shr 4)
        section?.setBlockSkyLight(x, y and 0x0f, z, level.toByte())
    }

    /**
     * Gets highest block in this (x,z)
     *
     * @param x the x 0~15
     * @param z the z 0~15
     */
    fun getHighestBlockAt(x: Int, z: Int): Int {
        for (y in dimensionData.maxHeight downTo dimensionData.minHeight) {
            if (getBlockState(x, y, z) !== BlockAir.properties.getBlockState()) {
                this.setHeightMap(x, z, y)
                return y
            }
        }
        return dimensionData.minHeight
    }

    /**
     * Recalculate height map for this chunk
     */
    fun recalculateHeightMapColumn(x: Int, z: Int): Int {
        val max = getHighestBlockAt(x, z)
        var y = max
        while (y >= 0) {
            val blockState = getBlockState(x, y, z, 0)
            val block = Block.get(blockState)
            if (block.lightFilter > 1 || block.diffusesSkyLight()) {
                break
            }
            --y
        }
        setHeightMap(x, z, y)
        return y
    }

    fun recalculateHeightMap() {
        for (z in 0..15) {
            for (x in 0..15) {
                this.recalculateHeightMapColumn(x, z)
            }
        }
    }

    fun getHeightMap(x: Int, z: Int): Int {
        return chunk.heightMapArray[(z shl 4) or x] + dimensionData.minHeight
    }

    fun setHeightMap(x: Int, z: Int, value: Int) {
        chunk.heightMapArray[(z shl 4) or x] = (value - dimensionData.minHeight).toShort()
    }

    fun getBiomeId(x: Int, y: Int, z: Int): Int {
        val section = getSection(y shr 4) ?: return 0
        return section.getBiomeId(x, y and 0x0f, z)
    }

    fun setBiomeId(x: Int, y: Int, z: Int, biomeId: Int) {
        setChanged()
        getOrCreateSection(y shr 4)!!.setBiomeId(x, y and 0x0f, z, biomeId)
    }

    val heightMapArray: ShortArray
        get() = chunk.heightMapArray

    var x: Int
        get() = chunk.x
        set(x) {
            chunk.x = x
        }

    var z: Int
        get() = chunk.z
        set(z) {
            chunk.z = z
        }

    val index: Long
        get() = chunk.index

    val provider: LevelProvider
        get() = chunk.provider

    var chunkState: ChunkState
        get() = chunk.chunkState
        set(chunkState) {
            chunk.chunkState = chunkState
        }

    fun addEntity(entity: Entity) {
        chunk.addEntity(entity)
    }

    fun removeEntity(entity: Entity) {
        chunk.removeEntity(entity)
    }

    val entities: Map<Long, Entity>
        get() = chunk.entities

    fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return chunk.getBlockEntity(x, y, z)
    }

    val changes: Long
        get() = chunk.changes

    fun setPosition(x: Int, z: Int) {
        chunk.setPosition(x, z)
    }
}
