package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.inventory.InventoryHolder

/**
 * Semantic interface
 */
interface BlockEntityInventoryHolder : BlockEntityNameable, InventoryHolder {
    var inventoryTitle: String
        get() = name
        set(name) {
            this.name = (name)
        }
}
