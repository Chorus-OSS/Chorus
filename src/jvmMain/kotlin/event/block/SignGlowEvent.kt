package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class SignGlowEvent(block: Block, val player: Player, val isGlowing: Boolean) : BlockEvent(block),
    Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}