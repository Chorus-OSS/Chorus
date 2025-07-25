package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityDispenser
import org.chorus_oss.chorus.blockentity.BlockEntityNameable


class DispenserInventory(blockEntity: BlockEntityDispenser) :
    EjectableInventory(blockEntity, InventoryType.DISPENSER, 9) {
    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder as BlockEntityDispenser
}
