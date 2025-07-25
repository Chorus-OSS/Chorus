package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList


class LeavesDecayEvent(block: Block) : BlockEvent(block), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
