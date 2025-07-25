package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList


open class BlockFormEvent(block: Block, newState: Block) :
    BlockGrowEvent(block, newState) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
