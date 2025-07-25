package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerInvalidMoveEvent(player: Player, revert: Boolean) : PlayerEvent(), Cancellable {
    val isRevert: Boolean

    init {
        this.player = player
        this.isRevert = revert
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
