package org.chorus_oss.chorus.level.util

import org.chorus_oss.chorus.block.Block

/**
 * The block implementing this interface should have a block cache that can be accessed in parallel, and usually every tick will call clear.
 */
interface TickCachedBlockStore {
    fun clearCachedStore()

    fun saveIntoCachedStore(block: Block, x: Int, y: Int, z: Int, layer: Int)

    fun getFromCachedStore(x: Int, y: Int, z: Int, layer: Int): Block

    fun computeFromCachedStore(x: Int, y: Int, z: Int, layer: Int, cachedBlockComputer: CachedBlockComputer): Block

    interface CachedBlockComputer {
        fun compute(): Block
    }
}
