package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.jetbrains.annotations.ApiStatus


interface Inventory {
    /**
     * get the size of inventory
     */
    val size: Int

    /**
     * Get the maximum size allowed to be placed on a single slot, which cannot exceed the limit of the client.
     */
    /**
     * set the maximum size allowed to be placed on a single slot, which cannot exceed the limit of the client.
     */
    var maxStackSize: Int

    /**
     * Get the item at the specified index of the inventory
     *
     * @param index the index from 0 ~ getSize()-1
     * @return the item
     */
    fun getItem(index: Int): Item

    /**
     * 获取该库存指定索引处的未克隆的物品
     *
     *
     * 若调用方保证不会修改此方法返回的Item对象，则使用此方法将降低特定场景下Item::clone()造成的性能开销
     *
     * @param index the index
     * @return the item
     */
    @ApiStatus.Internal
    fun getUnclonedItem(index: Int): Item {
        //你需要覆写它来实现
        return getItem(index)
    }

    /**
     * @see .setItem
     */
    fun setItem(index: Int, item: Item): Boolean {
        return setItem(index, item, true)
    }

    /**
     * Sets the item at the specified index in this inventory
     *
     * @param index the index from 0 ~ getSize()-1
     * @param item  the item
     * @param send  Whether to send sync datapacket for client
     * @return the item
     */
    fun setItem(index: Int, item: Item, send: Boolean): Boolean

    /**
     * Add some items to the inventory
     *
     * @param slots the items
     * @return the remain items that can't add to the inventory
     */
    fun addItem(vararg slots: Item): Array<Item>

    /**
     * @param item the item
     * @return Is there still a space in this inventory that allows this item to be added?
     */
    fun canAddItem(item: Item): Boolean

    /**
     * remove some items to the inventory
     */
    fun removeItem(vararg slots: Item): Array<Item>

    /**
     * Get all items of the inventory
     *
     *
     * key = slot index (from 0 ~ getSize()-1)
     *
     *
     * value = item
     */
    /**
     * set all items of the inventory
     *
     *
     * key = slot index (from 0 ~ getSize()-1)
     *
     *
     * value = item
     */
    var contents: Map<Int, Item>

    /**
     * @see .sendContents
     */
    fun sendContents(player: Player)

    /**
     * @see .sendContents
     */
    fun sendContents(vararg players: Player)

    /**
     * Sync all changes of item for this inventory to the client
     *
     * @param players the target players to receive the packet
     */
    fun sendContents(players: Collection<Player>)

    /**
     * @see .sendSlot
     */
    fun sendSlot(index: Int, player: Player)

    /**
     * @see .sendSlot
     */
    fun sendSlot(index: Int, vararg players: Player)

    /**
     * Sync a change of item for this inventory to the client
     *
     * @param index   the slot index where this item is located
     * @param players the target players to receive the packet
     */
    fun sendSlot(index: Int, players: Collection<Player>)

    /**
     * Get the free space size of this item that can be place in this inventory
     */
    fun getFreeSpace(item: Item): Int

    /**
     * Whether this item exists in the inventory will detect AUX and NBT
     */
    fun contains(item: Item): Boolean


    /**
     * Gets a map of all items in this inventory that match this item
     */
    fun all(item: Item): Map<Int, Item>


    fun first(item: Item): Int {
        return first(item, false)
    }

    /**
     * Get the first slot index that matching this item located in this inventory
     *
     * @param item  the item
     * @param exact Whether to exact match the item count
     * @return the index of item
     */
    fun first(item: Item, exact: Boolean): Int

    /**
     * The first free space in this inventory
     */
    fun firstEmpty(item: Item?): Int

    /**
     * Decrease the item count in the slot index.
     */
    fun decreaseCount(slot: Int)


    /**
     * Remove all items matching this item from this inventory
     *
     * @param item the item
     */
    fun remove(item: Item)

    /**
     * @see .clear
     */
    fun clear(index: Int): Boolean {
        return clear(index, true)
    }

    /**
     * Remove all items in the index
     */
    fun clear(index: Int, send: Boolean): Boolean

    /**
     * Remove all items in the inventory
     */
    fun clearAll()

    val isFull: Boolean

    val isEmpty: Boolean

    val viewers: MutableSet<Player>

    val type: InventoryType

    val holder: InventoryHolder?

    fun onOpen(who: Player)

    @ApiStatus.Internal
    fun open(who: Player): Boolean

    fun onClose(who: Player)

    fun close(who: Player)

    /**
     * This method is called when [.setItem] is executed, and the item has been put into slots
     *
     * @param index  The grid index of the item's changes
     * @param before Items before the change
     * @param send   Whether to send InventorySlotPacket to the client
     */
    fun onSlotChange(index: Int, before: Item, send: Boolean)


    fun addListener(listener: InventoryListener)


    fun removeListener(listener: InventoryListener)

    @ApiStatus.Internal
    fun init() {
    }

    /**
     * native slot id <---> network slot id
     */
    @ApiStatus.Internal
    fun networkSlotMap(): BiMap<Int, Int> {
        return HashBiMap.create()
    }

    /**
     * slot id ---> ContainerSlotType
     */
    @ApiStatus.Internal
    fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        return HashMap()
    }

    @ApiStatus.Internal
    fun fromNetworkSlot(networkSlot: Int): Int {
        return networkSlotMap().inverse().getOrDefault(networkSlot, networkSlot)
    }

    @ApiStatus.Internal
    fun toNetworkSlot(nativeSlot: Int): Int {
        return networkSlotMap().getOrDefault(nativeSlot, nativeSlot)
    }

    @ApiStatus.Internal
    fun getSlotType(nativeSlot: Int): ContainerSlotType {
        val type = slotTypeMap()[fromNetworkSlot(nativeSlot)]
            ?: throw RuntimeException("ContainerSlotType $nativeSlot does not exist!")
        return type
    }

    companion object {
        const val MAX_STACK: Int = 64
    }
}
