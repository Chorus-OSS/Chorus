package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.lang.TextContainer

/**
 * Called when the player spawns in the world after logging in, when they first see the terrain.
 *
 *
 * Note: A lot of data is sent to the player between login and this event. Disconnecting the player during this event
 * will cause this data to be wasted. Prefer disconnecting at login-time if possible to minimize bandwidth wastage.
 *
 * @see PlayerLoginEvent
 */
class PlayerJoinEvent : PlayerEvent {
    @JvmField
    var joinMessage: TextContainer

    constructor(player: Player, joinMessage: TextContainer) {
        this.player = player
        this.joinMessage = joinMessage
    }

    constructor(player: Player, joinMessage: String) {
        this.player = player
        this.joinMessage = TextContainer(joinMessage)
    }

    fun setJoinMessage(joinMessage: String) {
        this.joinMessage = TextContainer(joinMessage)
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
