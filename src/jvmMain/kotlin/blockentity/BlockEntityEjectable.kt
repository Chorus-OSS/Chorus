package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.inventory.EjectableInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag

abstract class BlockEntityEjectable(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    BlockEntityInventoryHolder {
    override lateinit var inventory: EjectableInventory


    protected abstract fun createInventory(): EjectableInventory

    protected abstract val blockEntityName: String

    override fun loadNBT() {
        super.loadNBT()
        this.inventory = createInventory()

        if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
            namedTag.putList("Items", ListTag<CompoundTag>())
        }

        for (i in 0..<this.size) {
            inventory.setItem(i, this.getItem(i))
        }
    }

    val size: Int
        get() = 9

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

        if (item.isNothing || item.getCount() <= 0) {
            if (i >= 0) {
                namedTag.getList("Items").remove(i)
            }
        } else if (i < 0) {
            (namedTag.getList("Items", CompoundTag::class.java)).add(d)
        } else {
            (namedTag.getList("Items", CompoundTag::class.java)).add(i, d)
        }
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound

            if (this.hasName()) {
                c.put("CustomName", namedTag["CustomName"]!!)
            }

            return c
        }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putList("Items", ListTag<CompoundTag>())
        for (index in 0..<this.size) {
            this.setItem(index, inventory.getItem(index))
        }
    }

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else blockEntityName
        set(name) {
            if (name == null || name == "") {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    override fun onBreak(isSilkTouch: Boolean) {
        for (content in inventory.contents.values) {
            level.dropItem(this.position, content)
        }
    }
}
