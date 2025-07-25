package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.item.Item


fun interface InventoryListener {
    fun onInventoryChanged(inventory: Inventory?, oldItem: Item?, slot: Int)
}
