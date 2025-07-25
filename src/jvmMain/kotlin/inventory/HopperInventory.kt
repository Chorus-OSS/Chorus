package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityHopper
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class HopperInventory(hopper: BlockEntityHopper) : ContainerInventory(hopper, InventoryType.HOPPER, 5) {
    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.LEVEL_ENTITY
        }
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.INVENTORY
        }
        return map
    }

    override fun canCauseVibration(): Boolean {
        return true
    }
}
