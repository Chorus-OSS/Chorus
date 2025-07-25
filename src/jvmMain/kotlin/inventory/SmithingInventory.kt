package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockSmithingTable
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class SmithingInventory(blockSmithingTable: BlockSmithingTable) :
    ContainerInventory(blockSmithingTable, InventoryType.SMITHING_TABLE, 3), CraftTypeInventory, SoleInventory {
    override fun init() {
        val map = super.networkSlotMap()
        for (i in 0..<size) {
            map[i] = 51 + i
        }

        val map2 = super.slotTypeMap()
        map2[0] = ContainerSlotType.SMITHING_TABLE_INPUT
        map2[1] = ContainerSlotType.SMITHING_TABLE_MATERIAL
        map2[2] = ContainerSlotType.SMITHING_TABLE_TEMPLATE
    }

    var equipment: Item
        get() = getItem(EQUIPMENT)
        set(equipment) {
            setItem(EQUIPMENT, equipment)
        }

    var ingredient: Item
        get() = getItem(INGREDIENT)
        set(ingredient) {
            setItem(INGREDIENT, ingredient)
        }

    var template: Item
        get() = getItem(TEMPLATE)
        set(template) {
            setItem(TEMPLATE, template)
        }

    override fun onClose(who: Player) {
        super.onClose(who)

        who.giveItem(getItem(EQUIPMENT), getItem(INGREDIENT), getItem(TEMPLATE))

        this.clear(EQUIPMENT)
        this.clear(INGREDIENT)
        this.clear(TEMPLATE)
    }

    companion object {
        private const val EQUIPMENT = 0
        private const val INGREDIENT = 1
        private const val TEMPLATE = 2
    }
}
