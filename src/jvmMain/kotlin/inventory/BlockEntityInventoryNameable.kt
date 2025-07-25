package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityNameable

interface BlockEntityInventoryNameable : InventoryNameable {
    val blockEntityInventoryHolder: BlockEntityNameable?

    override var inventoryTitle: String
        get() = blockEntityInventoryHolder!!.name
        set(name) {
            blockEntityInventoryHolder!!.name = name
        }
}
