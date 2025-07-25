package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.AnvilInventory
import org.chorus_oss.chorus.item.Item

class RepairItemEvent(
    inventory: AnvilInventory,
    val oldItem: Item,
    val newItem: Item,
    val materialItem: Item,
    var cost: Int,
    val player: Player
) :
    InventoryEvent(inventory), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
