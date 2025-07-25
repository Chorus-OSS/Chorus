package org.chorus_oss.chorus.level.generator.stages

import org.chorus_oss.chorus.level.generator.ChunkGenerateContext
import org.chorus_oss.chorus.level.generator.GenerateStage

class LightPopulationStage : GenerateStage() {
    override fun name(): String {
        return NAME
    }

    override fun apply(context: ChunkGenerateContext) {
        val chunk = context.chunk
        chunk.recalculateHeightMap()
        chunk.populateSkyLight()
    }

    companion object {
        const val NAME: String = "light_population"
    }
}
