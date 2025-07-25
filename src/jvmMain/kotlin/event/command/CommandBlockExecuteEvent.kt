package org.chorus_oss.chorus.event.command

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.block.BlockEvent

class CommandBlockExecuteEvent(block: Block, var command: String) : BlockEvent(block), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
