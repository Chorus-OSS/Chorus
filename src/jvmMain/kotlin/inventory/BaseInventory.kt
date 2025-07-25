package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityInventoryChangeEvent
import org.chorus_oss.chorus.event.inventory.InventoryCloseEvent
import org.chorus_oss.chorus.event.inventory.InventoryOpenEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.network.protocol.types.inventory.FullContainerName
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.item.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min


abstract class BaseInventory(
    override var holder: InventoryHolder,
    override val type: InventoryType,
    override val size: Int
) : Inventory {
    protected val slots: HashMap<Int, Item> = HashMap()
    override val viewers: MutableSet<Player> = HashSet()
    override var maxStackSize: Int = Inventory.MAX_STACK
    protected var listeners: MutableList<InventoryListener>? = null
    protected var slotTypeMap: MutableMap<Int?, ContainerSlotType?> =
        HashMap()
    protected var networkSlotMap: BiMap<Int, Int> =
        HashBiMap.create()

    init {
        init()
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        return this.slotTypeMap
    }

    override fun networkSlotMap(): BiMap<Int, Int> {
        return this.networkSlotMap
    }

    override fun getItem(index: Int): Item {
        return if (slots.containsKey(index)) slots[index]!!.clone() else Item.AIR
    }

    override fun getUnclonedItem(index: Int): Item {
        return slots.getOrDefault(index, Item.AIR)
    }

    override var contents: Map<Int, Item>
        get() = HashMap(this.slots)
        set(items) {
            var items1: Map<Int, Item?> = items
            if (items1.size > this.size) {
                var newItems =
                    TreeMap(items1)
                items1 = newItems
                newItems = TreeMap()
                var i = 0
                for ((key, value) in items1) {
                    newItems[key] = value
                    i++
                    if (i >= this.size) {
                        break
                    }
                }
                items1 = newItems
            }

            for (i in 0..<this.size) {
                if (!items1.containsKey(i)) {
                    if (slots.containsKey(i)) {
                        this.clear(i)
                    }
                } else {
                    if (!this.setItem(i, items1[i]!!)) {
                        this.clear(i)
                    }
                }
            }
        }

    @ApiStatus.Internal
    fun setItemInternal(index: Int, item: Item) {
        slots[index] = item
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        var item1 = item
        if (index < 0 || index >= this.size) {
            return false
        } else if (item1.isNothing) {
            return this.clear(index, send)
        }

        item1 = item1.clone()
        val holder = this.holder
        if (holder is Entity) {
            val ev = EntityInventoryChangeEvent(holder, this.getItem(index), item1, index)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                this.sendSlot(index, this.viewers)
                return false
            }

            item1 = ev.newItem
        }

        if (holder is BlockEntity) {
            holder.setDirty()
        }

        val old = this.getUnclonedItem(index)
        slots[index] = item1
        this.onSlotChange(index, old, send)

        return true
    }

    override fun contains(item: Item): Boolean {
        var count = max(1.0, item.getCount().toDouble()).toInt()
        val checkDamage = item.hasMeta() && item.damage >= 0
        val checkTag = true
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
        val checkTag = item.compoundTag.isNotEmpty()
        for ((key, value) in this.contents) {
            if (item.equals(value, checkDamage, checkTag)) {
                slots[key] = value
            }
        }

        return slots
    }

    override fun remove(item: Item) {
        val checkDamage = item.hasMeta()
        val checkTag = item.compoundTag.isNotEmpty()
        for ((key, value) in this.contents) {
            if (item.equals(value, checkDamage, checkTag)) {
                this.clear(key)
            }
        }
    }

    override fun first(item: Item, exact: Boolean): Int {
        val count = max(1.0, item.getCount().toDouble()).toInt()
        val checkDamage = item.hasMeta()
        val checkTag = item.compoundTag.isNotEmpty()
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
        for (i in 0..<this.size) {
            if (getUnclonedItem(i).isNothing) {
                return i
            }
        }

        return -1
    }

    override fun decreaseCount(slot: Int) {
        var item = this.getUnclonedItem(slot)

        if (item.getCount() > 0) {
            item = item.clone()
            item.count--
            this.setItem(slot, item)
        }
    }

    override fun canAddItem(item: Item): Boolean {
        var item1 = item
        item1 = item1.clone()
        val checkDamage = item1.hasMeta()
        val checkTag = item1.compoundTag.isNotEmpty()
        for (i in 0..<this.size) {
            val slot = this.getUnclonedItem(i)
            if (item1.equals(slot, checkDamage, checkTag)) {
                val diff: Int
                if (((min(slot.maxStackSize.toDouble(), maxStackSize.toDouble()) - slot.getCount()).also {
                        diff =
                            it.toInt()
                    }) > 0) {
                    item1.setCount(item1.getCount() - diff)
                }
            } else if (slot.isNothing) {
                item1.setCount(
                    (item1.getCount() - min(
                        slot.maxStackSize.toDouble(),
                        maxStackSize.toDouble()
                    )).toInt()
                )
            }

            if (item1.getCount() <= 0) {
                return true
            }
        }

        return false
    }

    override fun addItem(vararg slots: Item): Array<Item> {
        val itemSlots: MutableList<Item> = ArrayList()
        for (slot in slots) {
            if (!slot.isNothing) {
                // Todo: clone only if necessary
                itemSlots.add(slot.clone())
            }
        }

        val emptySlots = mutableListOf<Int>()

        for (i in 0..<this.size) {
            //获取未克隆Item对象
            var item = this.getUnclonedItem(i)
            if (item.isNothing || item.getCount() <= 0) {
                emptySlots.add(i)
            }

            //使用迭代器而不是新建一个ArrayList
            val iterator = itemSlots.iterator()
            while (iterator.hasNext()) {
                val slot = iterator.next()
                if (slot == item) {
                    val maxStackSize = min(maxStackSize.toDouble(), item.maxStackSize.toDouble()).toInt()
                    if (item.getCount() < maxStackSize) {
                        var amount =
                            min((maxStackSize - item.getCount()).toDouble(), slot.getCount().toDouble()).toInt()
                        amount = min(amount.toDouble(), this.maxStackSize.toDouble()).toInt()
                        if (amount > 0) {
                            //在需要clone时再clone
                            item = item.clone()
                            slot.setCount(slot.getCount() - amount)
                            item.setCount(item.getCount() + amount)
                            this.setItem(i, item)
                            if (slot.getCount() <= 0) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
            if (itemSlots.isEmpty()) {
                break
            }
        }

        if (itemSlots.isNotEmpty() && !emptySlots.isEmpty()) {
            for (slotIndex in emptySlots.iterator()) {
                if (itemSlots.isNotEmpty()) {
                    val slot = itemSlots[0]
                    val maxStackSize = min(
                        slot.maxStackSize.toDouble(),
                        maxStackSize.toDouble()
                    ).toInt()
                    var amount = min(maxStackSize.toDouble(), slot.getCount().toDouble()).toInt()
                    amount = min(amount.toDouble(), this.maxStackSize.toDouble()).toInt()
                    slot.setCount(slot.getCount() - amount)
                    val item = slot.clone()
                    item.setCount(amount)
                    this.setItem(slotIndex, item)
                    if (slot.getCount() <= 0) {
                        itemSlots.remove(slot)
                    }
                }
            }
        }

        return itemSlots.toTypedArray()
    }

    override fun removeItem(vararg slots: Item): Array<Item> {
        val itemSlots: MutableList<Item> = ArrayList()
        for (slot in slots) {
            if (!slot.isNothing) {
                itemSlots.add(slot.clone())
            }
        }

        for (i in 0..<this.size) {
            var item = this.getUnclonedItem(i)
            if (item.isNothing || item.getCount() <= 0) {
                continue
            }

            val iterator = itemSlots.iterator()
            while (iterator.hasNext()) {
                val slot = iterator.next()
                if (slot.equals(item, item.hasMeta(), item.compoundTag.isNotEmpty())) {
                    item = item.clone()
                    val amount = min(item.getCount().toDouble(), slot.getCount().toDouble()).toInt()
                    slot.setCount(slot.getCount() - amount)
                    item.setCount(item.getCount() - amount)
                    this.setItem(i, item)
                    if (slot.getCount() <= 0) {
                        iterator.remove()
                    }
                }
            }

            if (itemSlots.isEmpty()) {
                break
            }
        }

        return itemSlots.toTypedArray()
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        if (slots.containsKey(index)) {
            var item = Item.AIR
            val old = slots[index]
            val holder = this.holder
            if (holder is Entity) {
                val ev = EntityInventoryChangeEvent(
                    holder as Entity,
                    old!!, item, index
                )
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    this.sendSlot(index, this.viewers)
                    return false
                }
                item = ev.newItem
            }

            if (!item.isNothing) {
                slots[index] = item.clone()
            } else {
                slots.remove(index)
            }

            this.onSlotChange(index, old!!, send)
        }

        return true
    }

    override fun clearAll() {
        for (index in contents.keys) {
            this.clear(index)
        }
    }

    override fun open(who: Player): Boolean {
        val ev = InventoryOpenEvent(this, who)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }
        this.onOpen(who)

        return true
    }

    override fun close(who: Player) {
        val ev = InventoryCloseEvent(this, who)
        Server.instance.pluginManager.callEvent(ev)
        this.onClose(who)
    }

    override fun onOpen(who: Player) {
        viewers.add(who)
    }

    override fun onClose(who: Player) {
        viewers.remove(who)
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        if (send) {
            this.sendSlot(index, this.viewers)
        }

        val blockHolder = holder
        if (blockHolder is BlockEntity) {
            blockHolder.setDirty()
        }

        if (before.id == ItemID.LODESTONE_COMPASS || getUnclonedItem(index).id == ItemID.LODESTONE_COMPASS) {
            val playerHolder = holder
            if (playerHolder is Player) {
                playerHolder.updateTrackingPositions(true)
            }
            viewers.forEach(Consumer { p: Player? -> p!!.updateTrackingPositions(true) })
        }


        if (this.listeners != null) {
            for (listener in listeners!!) {
                listener.onInventoryChanged(this, before, index)
            }
        }
    }

    override fun sendContents(player: Player) {
        this.sendContents(*arrayOf(player))
    }

    override fun sendContents(vararg players: Player) {
        for (player in players) {
            val id = player.getWindowId(this)
            if (id == -1 || !player.spawned) {
                this.close(player)
                continue
            }

            val packet = org.chorus_oss.protocol.packets.InventoryContentPacket(
                windowID = id.toUInt(),
                content = List(this.size) { ItemStack(this.getUnclonedItem(it)) },
                container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                    org.chorus_oss.protocol.types.itemstack.ContainerSlotType.AnvilInput,
                    null
                ),
                storageItem = ItemStack(Item.AIR)
            )

            player.sendPacket(packet)
        }
    }

    override val isFull: Boolean
        get() {
            if (slots.size < this.size) {
                return false
            }

            for (item in slots.values) {
                if (item.isNothing || item.getCount() < item.maxStackSize || item.getCount() < this.maxStackSize) {
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

            for (item in slots.values) {
                if (!item.isNothing) {
                    return false
                }
            }

            return true
        }

    /**
     * 检测指定物品能在该库存所能存放的空余数量
     *
     * @param item 要检测的物品
     * @return 所能存放的空余数量
     */
    override fun getFreeSpace(item: Item): Int {
        val maxStackSize = min(
            item.maxStackSize.toDouble(),
            maxStackSize.toDouble()
        ).toInt()
        var space = (this.size - slots.size) * maxStackSize

        for (slot in contents.values) {
            if (slot.isNothing) {
                space += maxStackSize
                continue
            }
            if (slot.equals(item, checkDamage = true, checkCompound = true)) {
                space += maxStackSize - slot.getCount()
            }
        }
        return space
    }

    override fun sendContents(players: Collection<Player>) {
        this.sendContents(*players.toTypedArray())
    }

    override fun sendSlot(index: Int, player: Player) {
        this.sendSlot(index, *arrayOf(player))
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        val slot = toNetworkSlot(index)

        for (player in players) {
            val id = player.getWindowId(this)
            if (id == -1) {
                this.close(player)
                continue
            }

            val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                windowID = id.toUInt(),
                slot = slot.toUInt(),
                container = org.chorus_oss.protocol.types.inventory.FullContainerName.invoke(
                    FullContainerName(
                        this.getSlotType(slot),
                        id
                    )
                ),
                storageItem = ItemStack(Item.AIR),
                newItem = ItemStack(this.getUnclonedItem(index))
            )

            player.sendPacket(packet)
        }
    }

    override fun sendSlot(index: Int, players: Collection<Player>) {
        this.sendSlot(index, *players.toTypedArray())
    }


    override fun addListener(listener: InventoryListener) {
        if (this.listeners == null) {
            this.listeners = ArrayList()
        }
        listeners!!.add(listener)
    }


    override fun removeListener(listener: InventoryListener) {
        if (this.listeners != null) {
            listeners!!.remove(listener)
        }
    }

    fun isValidSlot(index: Int): Boolean {
        return index >= 0 && index < slots.size
    }
}
