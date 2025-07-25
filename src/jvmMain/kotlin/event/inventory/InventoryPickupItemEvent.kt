package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.Inventory


class InventoryPickupItemEvent(inventory: Inventory, val item: EntityItem) : InventoryEvent(inventory),
    Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
