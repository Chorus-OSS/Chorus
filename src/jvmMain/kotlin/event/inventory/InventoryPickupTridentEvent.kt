package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityThrownTrident
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.Inventory

class InventoryPickupTridentEvent(inventory: Inventory, val trident: EntityThrownTrident) :
    InventoryEvent(inventory), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
