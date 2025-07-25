package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerToggleFlightEvent(player: Player, isFlying: Boolean) : PlayerEvent(), Cancellable {
    @JvmField
    val isFlying: Boolean

    init {
        this.player = player
        this.isFlying = isFlying
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
