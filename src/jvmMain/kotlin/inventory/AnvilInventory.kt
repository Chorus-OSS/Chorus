package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockAnvil
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class AnvilInventory(anvil: BlockAnvil) //2 INPUT, 1 OUTPUT
    : ContainerInventory(anvil, InventoryType.ANVIL, 3), CraftTypeInventory, SoleInventory {
    override fun networkSlotMap(): BiMap<Int, Int> {
        val map = super.networkSlotMap()
        map[0] = 1 //INPUT
        map[1] = 2 //MATERIAL
        map[2] = 3 //OUTPUT
        return map
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.ANVIL_INPUT
        map[1] = ContainerSlotType.ANVIL_MATERIAL
        map[2] = ContainerSlotType.ANVIL_RESULT
        return map
    }

    override fun onClose(who: Player) {
        super.onClose(who)

        var drops = arrayOf(
            inputSlot,
            materialSlot
        )
        drops = who.inventory.addItem(*drops)
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                holder.level!!.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }

        clear(INPUT)
        clear(MATERIAL)
    }

    val inputSlot: Item
        get() = this.getItem(INPUT)

    val materialSlot: Item
        get() = this.getItem(MATERIAL)

    val outputSlot: Item
        get() = this.getItem(OUTPUT)

    fun setInputSlot(item: Item?, send: Boolean): Boolean {
        return setItem(INPUT, item!!, send)
    }

    fun setInputSlot(item: Item?): Boolean {
        return setInputSlot(item, true)
    }

    fun setMaterialSlot(item: Item?, send: Boolean): Boolean {
        return setItem(MATERIAL, item!!, send)
    }

    fun setMaterialSlot(item: Item?): Boolean {
        return setMaterialSlot(item, true)
    }

    fun setOutputSlot(item: Item?, send: Boolean): Boolean {
        return setItem(OUTPUT, item!!, send)
    }

    fun setOutputSlot(item: Item?): Boolean {
        return setOutputSlot(item, true)
    }

    override fun sendContents(vararg players: Player) {
        for (slot in 0..<(size - 1)) {
            sendSlot(slot, *players)
        }
    }

    companion object {
        const val INPUT: Int = 0
        const val MATERIAL: Int = 1
        const val OUTPUT: Int = 2
    }
}
