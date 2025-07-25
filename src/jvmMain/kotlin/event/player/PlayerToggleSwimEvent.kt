package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerToggleSwimEvent(player: Player, isSwimming: Boolean) : PlayerEvent(), Cancellable {
    val isSwimming: Boolean

    init {
        this.player = player
        this.isSwimming = isSwimming
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
