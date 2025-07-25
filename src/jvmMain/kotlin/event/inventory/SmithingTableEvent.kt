package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.SmithingInventory
import org.chorus_oss.chorus.item.Item

class SmithingTableEvent(
    inventory: SmithingInventory,
    val equipmentItem: Item,
    val resultItem: Item,
    val ingredientItem: Item,
    val player: Player
) :
    InventoryEvent(inventory), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
