package org.chorus_oss.chorus.level.generator

import com.google.common.base.Preconditions
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.IChunk
import java.util.function.Consumer

abstract class Generator(val dimensionData: DimensionData, val settings: Map<String?, Any>?) :
    BlockID {
    protected val start: GenerateStage
    protected val end: GenerateStage?
    var level: Level? = null

    init {
        val builder = GenerateStage.Builder()
        stages(builder)
        this.start = builder.start
        this.end = builder.end
    }

    abstract fun stages(builder: GenerateStage.Builder)

    abstract val name: String

    @JvmOverloads
    fun syncGenerate(chunk: IChunk, to: String? = end!!.name()): IChunk {
        val context = ChunkGenerateContext(this, level, chunk)
        var future = start.scope.async {
            start.apply(context)
        }
        var now: GenerateStage = start
        while (true) {
            now = now.nextStage ?: break
            if (now.name() == to) {
                future = now.scope.async {
                    future.await()
                    now.apply(context)
                }
                break
            }
            future = now.scope.async {
                future.await()
                now.apply(context)
            }
        }
        runBlocking { future.await() }
        return context.chunk
    }

    fun asyncGenerate(chunk: IChunk, callback: Consumer<ChunkGenerateContext>) {
        asyncGenerate(chunk, end!!.name(), callback)
    }

    @JvmOverloads
    fun asyncGenerate(
        chunk: IChunk,
        to: String? = end!!.name(),
        callback: Consumer<ChunkGenerateContext> = Consumer { }
    ) {
        Preconditions.checkNotNull(to)
        val context = ChunkGenerateContext(this, level, chunk)
        asyncGenerate0(context, start, to) { callback.accept(context) }
    }

    private fun asyncGenerate0(context: ChunkGenerateContext, start: GenerateStage?, to: String?, callback: Runnable) {
        if (start == null || to == null) return
        if (to == start.name()) {
            start.scope.launch {
                start.apply(context)
                callback.run()
            }
            return
        }
        start.scope.launch {
            start.apply(context)
            asyncGenerate0(context, start.nextStage, to, callback)
        }
    }
}
