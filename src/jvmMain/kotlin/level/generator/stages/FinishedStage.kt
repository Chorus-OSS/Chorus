package org.chorus_oss.chorus.level.generator.stages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.chorus_oss.chorus.level.format.ChunkState
import org.chorus_oss.chorus.level.generator.ChunkGenerateContext
import org.chorus_oss.chorus.level.generator.GenerateStage

class FinishedStage : GenerateStage() {
    override fun apply(context: ChunkGenerateContext) {
        val chunk = context.chunk
        chunk.chunkState = ChunkState.FINISHED
    }

    override val scope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Unconfined)

    override fun name(): String {
        return NAME
    }

    companion object {
        const val NAME: String = "finished"
    }
}
