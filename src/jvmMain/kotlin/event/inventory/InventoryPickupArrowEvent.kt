package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.Inventory


class InventoryPickupArrowEvent(inventory: Inventory, val arrow: EntityArrow) : InventoryEvent(inventory),
    Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
