package org.chorus_oss.chorus.level.generator.stages

import org.chorus_oss.chorus.block.BlockBedrock
import org.chorus_oss.chorus.block.BlockDirt
import org.chorus_oss.chorus.block.BlockGrassBlock
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.level.biome.BiomeID
import org.chorus_oss.chorus.level.format.ChunkState
import org.chorus_oss.chorus.level.generator.ChunkGenerateContext
import org.chorus_oss.chorus.level.generator.GenerateStage

class FlatGenerateStage : GenerateStage() {
    override fun name(): String {
        return NAME
    }

    override fun apply(context: ChunkGenerateContext) {
        val chunk = context.chunk
        for (x in 0..15) {
            for (z in 0..15) {
                chunk.setHeightMap(x, z, 5)
                for (y in 0..4) {
                    when (y) {
                        0 -> chunk.setBlockState(x, y, z, bedrock)
                        4 -> chunk.setBlockState(x, y, z, grass)
                        else -> chunk.setBlockState(x, y, z, dirt)
                    }
                }
                for (i in context.generator.dimensionData.minSectionY..<context.generator.dimensionData.minSectionY) {
                    chunk.setBiomeId(x, i, z, BiomeID.Companion.PLAINS)
                }
            }
        }
        chunk.chunkState = ChunkState.POPULATED
    }

    companion object {
        const val NAME: String = "flat_generate"

        val bedrock: BlockState = BlockBedrock.properties.defaultState
        val grass: BlockState = BlockGrassBlock.properties.defaultState
        val dirt: BlockState = BlockDirt.properties.defaultState
    }
}
