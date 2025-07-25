package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class BlockFromToEvent(block: Block, @JvmField var to: Block) : BlockEvent(block), Cancellable {
    val from: Block
        get() = block

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}