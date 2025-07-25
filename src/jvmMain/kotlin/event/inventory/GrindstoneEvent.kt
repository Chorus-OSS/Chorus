package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.GrindstoneInventory
import org.chorus_oss.chorus.item.Item

class GrindstoneEvent(
    inventory: GrindstoneInventory,
    val firstItem: Item,
    @JvmField val resultItem: Item,
    val secondItem: Item,
    @JvmField var experienceDropped: Int,
    val player: Player
) :
    InventoryEvent(inventory), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
