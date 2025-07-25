package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList


class BlockSpreadEvent(block: Block, val source: Block, newState: Block) :
    BlockFormEvent(block, newState) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
