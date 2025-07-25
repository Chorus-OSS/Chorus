package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList

class PlayerIllegalFlightEvent(player: Player) : PlayerEvent() {
    var isKick: Boolean = true

    init {
        this.player = player
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
