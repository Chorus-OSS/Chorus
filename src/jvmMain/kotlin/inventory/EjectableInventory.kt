package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType


abstract class EjectableInventory(holder: InventoryHolder, type: InventoryType, size: Int) :
    ContainerInventory(holder, type, size), BlockEntityInventoryNameable {
    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.LEVEL_ENTITY
        }
    }
}
