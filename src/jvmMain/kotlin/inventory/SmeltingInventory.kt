package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityFurnace
import org.chorus_oss.chorus.item.Item

abstract class SmeltingInventory(holder: BlockEntityFurnace, type: InventoryType, size: Int) :
    ContainerInventory(holder, type, size) {
    val result: Item
        get() = this.getItem(2)

    val fuel: Item
        get() = this.getItem(1)

    val smelting: Item
        get() = this.getItem(0)

    fun setResult(item: Item?): Boolean {
        return this.setItem(2, item!!)
    }

    fun setFuel(item: Item?): Boolean {
        return this.setItem(1, item!!)
    }

    fun setSmelting(item: Item?): Boolean {
        return this.setItem(0, item!!)
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        super.onSlotChange(index, before, send)
        if (index == 2 && (before.isNothing || before.getCount() > 0)) {
            val holder = holder as BlockEntityFurnace
            val xp = holder.calculateXpDrop()
            if (xp > 0) {
                holder.storedXP = 0f
                holder.level.dropExpOrb(holder.position, xp.toInt())
            }
        }
        (holder as BlockEntityFurnace).scheduleUpdate()
    }
}
