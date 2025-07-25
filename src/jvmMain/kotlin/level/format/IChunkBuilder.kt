package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.nbt.tag.CompoundTag

interface IChunkBuilder {
    fun chunkX(chunkX: Int): IChunkBuilder

    val chunkX: Int

    fun chunkZ(chunkZ: Int): IChunkBuilder

    val chunkZ: Int

    fun state(state: ChunkState): IChunkBuilder

    fun levelProvider(levelProvider: LevelProvider): IChunkBuilder

    val dimensionData: DimensionData

    fun sections(sections: Array<SubChunk?>): IChunkBuilder

    val sections: Array<SubChunk?>

    fun heightMap(heightMap: ShortArray): IChunkBuilder

    fun entities(entities: List<CompoundTag>): IChunkBuilder

    fun blockEntities(blockEntities: List<CompoundTag>): IChunkBuilder

    fun build(): IChunk

    fun emptyChunk(chunkX: Int, chunkZ: Int): IChunk
}
