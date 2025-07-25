package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item

/**
 * Called when inventory transaction is not caused by a player
 */
class InventoryMoveItemEvent(
    from: Inventory,
    val targetInventory: Inventory,
    val source: InventoryHolder,
    var item: Item,
    val action: Action
) :
    InventoryEvent(from), Cancellable {
    enum class Action {
        SLOT_CHANGE,  //transaction between 2 inventories
        PICKUP,
        DROP,
        DISPENSE
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
