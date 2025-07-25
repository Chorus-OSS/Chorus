package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Locator

class BlockExplodeEvent(
    block: Block,
    val position: Locator,
    var affectedBlocks: Set<Block>,
    @JvmField var ignitions: Set<Block>,
    @JvmField var yield: Double,
    val fireChance: Double
) :
    BlockEvent(block), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
