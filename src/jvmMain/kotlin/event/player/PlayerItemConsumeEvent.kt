package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerItemConsumeEvent(player: Player, item: Item) : PlayerEvent(), Cancellable {
    private val item: Item

    init {
        this.player = player
        this.item = item
    }

    fun getItem(): Item {
        return item.clone()
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
