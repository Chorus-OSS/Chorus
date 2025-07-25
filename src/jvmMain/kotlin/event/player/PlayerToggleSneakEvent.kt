package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerToggleSneakEvent(player: Player, isSneaking: Boolean) : PlayerEvent(), Cancellable {
    val isSneaking: Boolean

    init {
        this.player = player
        this.isSneaking = isSneaking
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
