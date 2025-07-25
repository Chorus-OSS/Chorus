package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.AnvilInventory

class PlayerTypingAnvilInventoryEvent(
    val player: Player,
    inventory: AnvilInventory,
    val previousName: String?,
    val typedName: String
) : InventoryEvent(inventory) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
