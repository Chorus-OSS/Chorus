package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerDropItemEvent(player: Player, drop: Item) : PlayerEvent(), Cancellable {
    val item: Item

    init {
        this.player = player
        this.item = drop
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
