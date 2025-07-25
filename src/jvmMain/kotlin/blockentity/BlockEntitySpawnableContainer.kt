package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.inventory.ContainerInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.function.Consumer

abstract class BlockEntitySpawnableContainer(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    BlockEntityInventoryHolder {

    abstract override var inventory: Inventory

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
            namedTag.putList("Items", ListTag<CompoundTag>())
        }

        val list = namedTag.getList("Items") as ListTag<CompoundTag>
        for (compound in list.all) {
            val item = NBTIO.getItemHelper(compound)
            (inventory as? ContainerInventory)?.setItemInternal(compound.getByte("Slot").toInt(), item)
        }
    }

    override fun close() {
        if (!closed) {
            inventory.viewers.forEach(Consumer { p: Player -> p.removeWindow(this.inventory) })
            super.close()
        }
    }

    override fun onBreak(isSilkTouch: Boolean) {
        for (content in inventory.contents.values) {
            level.dropItem(this.position, content)
        }
        inventory.clearAll() // Stop items from being moved around by another player in the inventory
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putList("Items", ListTag<CompoundTag>())
        for (index in 0..<inventory.size) {
            this.setItem(index, inventory.getItem(index))
        }
    }

    protected fun getSlotIndex(index: Int): Int {
        val list = namedTag.getList("Items", CompoundTag::class.java)
        for (i in 0..<list.size()) {
            if (list[i].getByte("Slot").toInt() == index) {
                return i
            }
        }

        return -1
    }

    fun getItem(index: Int): Item {
        val i = this.getSlotIndex(index)
        if (i < 0) {
            return Item.AIR
        } else {
            val data = namedTag.getList("Items")[i] as CompoundTag
            return NBTIO.getItemHelper(data)
        }
    }

    fun setItem(index: Int, item: Item) {
        val i = this.getSlotIndex(index)

        val d = NBTIO.putItemHelper(item, index)

        // If item is air or count less than 0, remove the item from the "Items" list
        if (item.isNothing || item.getCount() <= 0) {
            if (i >= 0) {
                namedTag.getList("Items").remove(i)
            }
        } else if (i < 0) {
            // If it is less than i, then it is a new item, so we are going to add it at the end of the list
            (namedTag.getList("Items", CompoundTag::class.java)).add(d)
        } else {
            // If it is more than i, then it is an update on a inventorySlot, so we are going to overwrite the item in the list
            (namedTag.getList("Items", CompoundTag::class.java)).add(i, d)
        }
    }
}
