package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector3

class PlayerInteractEntityEvent(player: Player, entity: Entity, item: Item, clickedPos: Vector3) :
    PlayerEvent(), Cancellable {
    val entity: Entity
    val item: Item
    val clickedPos: Vector3

    init {
        this.player = player
        this.entity = entity
        this.item = item
        this.clickedPos = clickedPos
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
