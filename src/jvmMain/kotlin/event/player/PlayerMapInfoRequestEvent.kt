package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerMapInfoRequestEvent(player: Player, item: Item) : PlayerEvent(), Cancellable {
    val map: Item

    init {
        this.player = player
        this.map = item
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
