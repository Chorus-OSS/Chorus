package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockStonecutterBlock
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.chorus.recipe.Input

class StonecutterInventory(blockStonecutterBlock: BlockStonecutterBlock) :
    ContainerInventory(blockStonecutterBlock, InventoryType.STONECUTTER, 3), CraftTypeInventory, InputInventory {
    override fun onClose(who: Player) {
        super.onClose(who)
        val drops = who.inventory.addItem(this.getItem(0))
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                holder.level!!.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }
        this.clear(0)
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        if (index == 3) {
            slots[index] = item
            return true
        } else return false
    }

    override val input: Input
        get() = Input(
            1,
            1,
            arrayOf(arrayOf(getItem(3)))
        )

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[3] = ContainerSlotType.STONECUTTER_INPUT
        map[4] = ContainerSlotType.STONECUTTER_RESULT
        return map
    }
}
