package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockGrindstone
import org.chorus_oss.chorus.event.inventory.InventoryCloseEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class GrindstoneInventory(blockGrindstone: BlockGrindstone) :
    ContainerInventory(blockGrindstone, InventoryType.GRINDSTONE, 3), CraftTypeInventory, SoleInventory {
    override fun networkSlotMap(): BiMap<Int, Int> {
        val map = super.networkSlotMap()
        map[0] = 16 //INPUT
        map[1] = 17 //ADDITIONAL
        map[2] = 18 //RESULT
        return map
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.GRINDSTONE_INPUT
        map[1] = ContainerSlotType.GRINDSTONE_ADDITIONAL
        map[2] = ContainerSlotType.GRINDSTONE_RESULT
        return map
    }

    override fun close(who: Player) {
        val ev = InventoryCloseEvent(this, who)
        Server.instance.pluginManager.callEvent(ev)
        onClose(who)
    }

    override fun onClose(who: Player) {
        super.onClose(who)

        var drops = arrayOf(
            firstItem,
            secondItem
        )
        drops = who.inventory.addItem(*drops)
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                (holder as BlockGrindstone).level.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }

        clear(SLOT_FIRST_ITEM)
        clear(SLOT_SECOND_ITEM)
    }

    val firstItem: Item
        get() = getItem(SLOT_FIRST_ITEM)


    val secondItem: Item
        get() = getItem(SLOT_SECOND_ITEM)


    val result: Item
        get() = getItem(SLOT_RESULT)


    fun setFirstItem(item: Item, send: Boolean): Boolean {
        return setItem(SLOT_FIRST_ITEM, item, send)
    }


    fun setFirstItem(item: Item): Boolean {
        return setFirstItem(item, true)
    }


    fun setSecondItem(item: Item, send: Boolean): Boolean {
        return setItem(SLOT_SECOND_ITEM, item, send)
    }


    fun setSecondItem(item: Item): Boolean {
        return setSecondItem(item, true)
    }


    fun setResult(item: Item, send: Boolean): Boolean {
        return setItem(SLOT_RESULT, item, send)
    }


    fun setResult(item: Item): Boolean {
        return setResult(item, true)
    }

    override fun getItem(index: Int): Item {
        if (index < 0 || index > 3) {
            return Item.AIR
        }
        return super.getItem(index)
    }


    override fun getUnclonedItem(index: Int): Item {
        if (index < 0 || index > 3) {
            return Item.AIR
        }
        return super.getUnclonedItem(index)
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        if (index < 0 || index > 3) {
            return false
        }
        return super.setItem(index, item, send)
    }

    companion object {
        private const val SLOT_FIRST_ITEM = 0
        private const val SLOT_SECOND_ITEM = 1
        private const val SLOT_RESULT = 2
    }
}
