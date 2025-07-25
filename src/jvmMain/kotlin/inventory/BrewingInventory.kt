package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityBrewingStand
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.jetbrains.annotations.Range

class BrewingInventory(brewingStand: BlockEntityBrewingStand) :
    ContainerInventory(brewingStand, InventoryType.BREWING_STAND, 5), BlockEntityInventoryNameable {
    override fun init() {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.BREWING_INPUT
        map[1] = ContainerSlotType.BREWING_RESULT
        map[2] = ContainerSlotType.BREWING_RESULT
        map[3] = ContainerSlotType.BREWING_RESULT
        map[4] = ContainerSlotType.BREWING_FUEL
    }

    var ingredient: Item
        get() = getItem(0)
        set(item) {
            setItem(0, item)
        }

    fun setResult(index: @Range(from = 1, to = 3) Int, result: Item?) {
        setItem(index, result!!)
    }

    fun getResult(index: @Range(from = 1, to = 3) Int): Item {
        return getItem(index)
    }

    var fuel: Item
        get() = getItem(4)
        set(fuel) {
            setItem(4, fuel)
        }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        super.onSlotChange(index, before, send)

        if (index >= 1 && index <= 3) {
            (holder as BlockEntityBrewingStand).updateBlock()
        }

        (holder as BlockEntityBrewingStand).scheduleUpdate()
    }

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = (holder as BlockEntityBrewingStand)
}