package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList

class BlockRedstoneEvent(block: Block, val oldPower: Int, val newPower: Int) : BlockEvent(block) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
