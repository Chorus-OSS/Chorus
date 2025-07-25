package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerToggleSprintEvent(player: Player, isSprinting: Boolean) : PlayerEvent(), Cancellable {
    val isSprinting: Boolean

    init {
        this.player = player
        this.isSprinting = isSprinting
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
