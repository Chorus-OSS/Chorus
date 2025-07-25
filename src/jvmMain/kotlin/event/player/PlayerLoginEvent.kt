package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

/**
 * Called after the player has successfully authenticated, before it spawns. The player is on the loading screen when
 * this is called.
 * Cancelling this event will cause the player to be disconnected with the kick message set.
 */
class PlayerLoginEvent(player: Player, kickMessage: String) : PlayerEvent(), Cancellable {
    @JvmField
    var kickMessage: String

    init {
        this.player = player
        this.kickMessage = kickMessage
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
