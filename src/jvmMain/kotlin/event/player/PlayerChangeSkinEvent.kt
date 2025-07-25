package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerChangeSkinEvent(player: Player, skin: Skin) : PlayerEvent(), Cancellable {
    val skin: Skin

    init {
        this.player = player
        this.skin = skin
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
