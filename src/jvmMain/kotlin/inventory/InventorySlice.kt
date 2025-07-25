package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import kotlin.math.max
import kotlin.math.min

open class InventorySlice(private val rawInv: Inventory, var startSlot: Int, var endSlot: Int) : Inventory {
    private var slotTypeMap: Map<Int, ContainerSlotType>? = null
    private var networkSlotMap: BiMap<Int, Int>? = null

    override fun getSlotType(nativeSlot: Int): ContainerSlotType {
        return if (slotTypeMap != null) {
            slotTypeMap!![nativeSlot]!!
        } else rawInv.getSlotType(nativeSlot)
    }

    override fun toNetworkSlot(nativeSlot: Int): Int {
        return if (networkSlotMap != null) {
            networkSlotMap!!.getOrDefault(nativeSlot, nativeSlot)
        } else rawInv.toNetworkSlot(nativeSlot)
    }

    override fun fromNetworkSlot(networkSlot: Int): Int {
        return if (networkSlotMap != null) {
            networkSlotMap!!.inverse().getOrDefault(networkSlot, networkSlot)
        } else rawInv.fromNetworkSlot(networkSlot)
    }

    fun setNetworkMapping(map: Map<Int, ContainerSlotType>?, biMap: BiMap<Int, Int>?) {
        slotTypeMap = map
        networkSlotMap = biMap
    }

    override val size: Int
        get() = endSlot - startSlot

    override var maxStackSize: Int
        get() = rawInv.maxStackSize
        set(size) {
            rawInv.maxStackSize = size
        }

    override fun getItem(index: Int): Item {
        // check whether the index is in the range
        if (index < 0 || index >= size) {
            return Item.AIR
        }
        return rawInv.getItem(index + startSlot)
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        // check whether the index is in the range
        if (index < 0 || index >= size) {
            return false
        }
        return rawInv.setItem(index + startSlot, item, send)
    }

    override fun addItem(vararg slots: Item): Array<Item> {
        return rawInv.addItem(*slots)
    }

    override fun canAddItem(item: Item): Boolean {
        var item1 = item
        item1 = item1.clone()
        val checkDamage = item1.hasMeta()
        val checkTag = item1.namedTag != null
        for (i in startSlot..<endSlot) {
            val slot = rawInv.getItem(i)
            if (item1.equals(slot, checkDamage, checkTag)) {
                val diff: Int
                if (((min(slot.maxStackSize.toDouble(), rawInv.maxStackSize.toDouble()) - slot.getCount()).also {
                        diff =
                            it.toInt()
                    }) > 0) {
                    item1.setCount(item1.getCount() - diff)
                }
            } else if (slot.isNothing) {
                item1.setCount(
                    (item1.getCount() - min(
                        slot.maxStackSize.toDouble(),
                        rawInv.maxStackSize.toDouble()
                    )).toInt()
                )
            }

            if (item1.getCount() <= 0) {
                return true
            }
        }
        return false
    }

    override fun removeItem(vararg slots: Item): Array<Item> {
        return rawInv.removeItem(*slots)
    }

    override var contents: Map<Int, Item>
        get() {
            val map = HashMap<Int, Item>()
            for (i in startSlot..<endSlot) {
                map[i - startSlot] = rawInv.getItem(i)
            }
            return map
        }
        set(items) {
            for ((key, value) in items) {
                // check whether the index is in the range
                if (key < 0 || key >= size) {
                    continue
                }
                rawInv.setItem(key + startSlot, value)
            }
        }

    override fun sendContents(player: Player) {
        rawInv.sendContents(player)
    }

    override fun sendContents(vararg players: Player) {
        rawInv.sendContents(*players)
    }

    override fun sendContents(players: Collection<Player>) {
        rawInv.sendContents(players)
    }

    override fun sendSlot(index: Int, player: Player) {
        rawInv.sendSlot(index + startSlot, player)
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        rawInv.sendSlot(index + startSlot, *players)
    }

    override fun sendSlot(index: Int, players: Collection<Player>) {
        rawInv.sendSlot(index + startSlot, players)
    }

    override fun getFreeSpace(item: Item): Int {
        return rawInv.getFreeSpace(item)
    }

    override fun contains(item: Item): Boolean {
        var count = max(1.0, item.getCount().toDouble()).toInt()
        val checkDamage = item.hasMeta() && item.damage >= 0
        val checkTag = item.compoundTag != null
        for (i in contents.values) {
            if (item.equals(i, checkDamage, checkTag)) {
                count -= i.getCount()
                if (count <= 0) {
                    return true
                }
            }
        }

        return false
    }

    override fun all(item: Item): Map<Int, Item> {
        val slots: MutableMap<Int, Item> = HashMap()
        val checkDamage = item.hasMeta() && item.damage >= 0
        val checkTag = item.compoundTag != null
        for ((key, value) in this.contents) {
            if (item.equals(value, checkDamage, checkTag)) {
                slots[key] = value
            }
        }

        return slots
    }

    override fun first(item: Item, exact: Boolean): Int {
        val count = max(1.0, item.getCount().toDouble()).toInt()
        val checkDamage = item.hasMeta()
        val checkTag = item.compoundTag != null
        for ((key, value) in this.contents) {
            if (item.equals(
                    value,
                    checkDamage,
                    checkTag
                ) && (value.getCount() == count || (!exact && value.getCount() > count))
            ) {
                return key
            }
        }

        return -1
    }

    override fun firstEmpty(item: Item?): Int {
        for (i in startSlot..<endSlot) {
            if (rawInv.getItem(i).isNothing) {
                return i
            }
        }

        return -1
    }

    override fun decreaseCount(slot: Int) {
        // check whether the index is in the range
        require(!(slot < 0 || slot >= size)) { "Slot index $slot out of range" }
        rawInv.decreaseCount(slot + startSlot)
    }

    override fun remove(item: Item) {
        val checkDamage = item.hasMeta()
        val checkTag = item.compoundTag != null
        for ((key, value) in this.contents) {
            if (item.equals(value, checkDamage, checkTag)) {
                this.clear(key)
            }
        }
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        return rawInv.clear(index + startSlot, send)
    }

    override fun clearAll() {
        for (i in startSlot..<endSlot) {
            rawInv.clear(i)
        }
    }

    override val isFull: Boolean
        get() {
            for (i in startSlot..<endSlot) {
                val item = rawInv.getItem(i)
                if (item == null || item.isNothing || item.getCount() < item.maxStackSize || item.getCount() < this.maxStackSize) {
                    return false
                }
            }

            return true
        }

    override val isEmpty: Boolean
        get() {
            if (this.maxStackSize <= 0) {
                return false
            }

            for (item in contents.values) {
                if (item != null && !item.isNothing) {
                    return false
                }
            }

            return true
        }

    override val viewers: MutableSet<Player>
        get() = rawInv.viewers

    override val type: InventoryType
        get() = rawInv.type

    override val holder: InventoryHolder?
        get() = rawInv.holder

    override fun onOpen(who: Player) {
        rawInv.onOpen(who)
    }

    override fun open(who: Player): Boolean {
        return rawInv.open(who)
    }

    override fun close(who: Player) {
        rawInv.close(who)
    }

    override fun onClose(who: Player) {
        rawInv.onClose(who)
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        // check whether the index is in the range
        require(!(index < 0 || index >= size)) { "Slot index $index out of range" }
        rawInv.onSlotChange(index + startSlot, before, send)
    }


    override fun addListener(listener: InventoryListener) {
        rawInv.addListener((InventoryListener { _, oldItem, slot ->
            listener.onInventoryChanged(
                this, oldItem, slot - startSlot
            )
        }))
    }


    override fun removeListener(listener: InventoryListener) {
        rawInv.removeListener((InventoryListener { _, oldItem, slot ->
            listener.onInventoryChanged(
                this, oldItem, slot - startSlot
            )
        }))
    }
}
