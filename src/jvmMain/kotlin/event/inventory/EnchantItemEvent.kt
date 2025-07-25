package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.EnchantInventory
import org.chorus_oss.chorus.item.Item

class EnchantItemEvent(
    inventory: EnchantInventory,
    var oldItem: Item,
    var newItem: Item,
    var xpCost: Int,
    var enchanter: Player
) :
    InventoryEvent(inventory), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
