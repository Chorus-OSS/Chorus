package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.entity.item.EntityChestMinecart


class MinecartChestInventory(minecart: EntityChestMinecart) :
    ContainerInventory(minecart, InventoryType.MINECART_CHEST, 27) {
    override fun canCauseVibration(): Boolean {
        return true
    }
}
