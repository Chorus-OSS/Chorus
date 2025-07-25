package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.chorus.recipe.Input

class CraftingGridInventory(holder: Player) : BaseInventory(holder, InventoryType.INVENTORY, 4),
    InputInventory {
    override fun init() {
        val map2 = super.slotTypeMap()
        for (i in 0..<size) {
            map2[i] = ContainerSlotType.CRAFTING_INPUT
        }

        val map1 = super.networkSlotMap()
        for (i in 0..<size) {
            map1[i] = 28 + i
        }
    }

    override val input: Input
        get() {
            val item1 = arrayOf(getItem(0), getItem(1))
            val item2 = arrayOf(getItem(2), getItem(3))
            val items = arrayOf(item1, item2)
            return Input(2, 2, items)
        }
}
