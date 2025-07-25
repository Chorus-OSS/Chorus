package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityDropper
import org.chorus_oss.chorus.blockentity.BlockEntityNameable


class DropperInventory(blockEntity: BlockEntityDropper) : EjectableInventory(blockEntity, InventoryType.DROPPER, 9) {
    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder as BlockEntityDropper
}
