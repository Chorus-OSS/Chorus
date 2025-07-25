package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerChangeArmorStandEvent(player: Player, armorStand: Entity, item: Item, slot: Int) :
    PlayerEvent(), Cancellable {
    val armorStand: Entity
    val slot: Int
    var item: Item

    init {
        this.player = player
        this.armorStand = armorStand
        this.item = item
        this.slot = slot
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
