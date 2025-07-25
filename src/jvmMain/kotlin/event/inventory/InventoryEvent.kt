package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.inventory.Inventory


abstract class InventoryEvent(val inventory: Inventory) : Event() {
    val viewers: Array<Player>
        get() = inventory.viewers.toTypedArray()
}
