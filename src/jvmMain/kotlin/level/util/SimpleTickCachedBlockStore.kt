package org.chorus_oss.chorus.level.util

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.util.TickCachedBlockStore.CachedBlockComputer
import java.util.concurrent.ConcurrentHashMap

class SimpleTickCachedBlockStore(private val level: Level) : TickCachedBlockStore {
    private val tickCachedBlockStore =
        ConcurrentHashMap<Int, Block?>(32, 0.75f)

    override fun clearCachedStore() {
        tickCachedBlockStore.clear()
    }

    override fun saveIntoCachedStore(block: Block, x: Int, y: Int, z: Int, layer: Int) {
        tickCachedBlockStore[Level.Companion.localBlockHash(x, y, z, layer, level)] = block
    }

    override fun getFromCachedStore(x: Int, y: Int, z: Int, layer: Int): Block {
        return tickCachedBlockStore[Level.localBlockHash(x, y, z, layer, level)]!!
    }

    override fun computeFromCachedStore(
        x: Int,
        y: Int,
        z: Int,
        layer: Int,
        cachedBlockComputer: CachedBlockComputer
    ): Block {
        return tickCachedBlockStore.computeIfAbsent(
            Level.localBlockHash(
                x,
                y,
                z,
                layer,
                level
            )
        ) { cachedBlockComputer.compute() }!!
    }
}
