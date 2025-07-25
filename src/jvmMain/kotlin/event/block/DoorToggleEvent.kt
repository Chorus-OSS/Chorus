package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList

class DoorToggleEvent(block: Block, @JvmField var player: Player?) : BlockUpdateEvent(block) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
