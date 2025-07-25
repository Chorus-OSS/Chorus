package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockLiquid
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class LiquidFlowEvent(val to: Block, val source: BlockLiquid, val newFlowDecay: Int) : BlockEvent(
    to
), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
