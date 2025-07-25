package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.HandlerList

class PlayerBedLeaveEvent(player: Player, bed: Block) : PlayerEvent() {
    val bed: Block

    init {
        this.player = player
        this.bed = bed
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
