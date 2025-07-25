package org.chorus_oss.chorus.event.redstone

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.block.BlockUpdateEvent

class RedstoneUpdateEvent(source: Block) : BlockUpdateEvent(source) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}

