package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerCommandPreprocessEvent(player: Player, message: String?) : PlayerMessageEvent(), Cancellable {
    init {
        this.player = player
        this.message = message
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
