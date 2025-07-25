package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList

class PlayerJumpEvent(player: Player) : PlayerEvent() {
    init {
        this.player = player
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
