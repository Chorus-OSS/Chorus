package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class EntityInventoryChangeEvent(entity: Entity, oldItem: Item, newItem: Item, slot: Int) :
    EntityEvent(), Cancellable {
    val oldItem: Item

    @JvmField
    var newItem: Item
    val slot: Int

    init {
        this.entity = entity
        this.oldItem = oldItem
        this.newItem = newItem
        this.slot = slot
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
