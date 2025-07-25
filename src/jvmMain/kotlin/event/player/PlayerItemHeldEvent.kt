package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerItemHeldEvent(player: Player, item: Item, hotbarSlot: Int) : PlayerEvent(),
    Cancellable {
    val item: Item
    val slot: Int

    init {
        this.player = player
        this.item = item
        this.slot = hotbarSlot
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
