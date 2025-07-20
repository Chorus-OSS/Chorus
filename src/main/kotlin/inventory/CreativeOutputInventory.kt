package org.chorus_oss.chorus.inventory

import com.google.common.base.Preconditions
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class CreativeOutputInventory(private val player: Player) : Inventory {
    private var item: Item = Item.AIR

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.CREATED_OUTPUT
        return map
    }

    override fun networkSlotMap(): BiMap<Int, Int> {
        val res = HashBiMap.create<Int, Int>()
        res[0] = 50
        return res
    }

    override val size: Int
        get() = 1

    override var maxStackSize: Int
        get() = 64
        set(size) {}

    override fun getItem(index: Int): Item {
        return item
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        this.item = item
        return true
    }

    fun setItem(item: Item): Boolean {
        return setItem(0, item, false)
    }

    override fun addItem(vararg slots: Item): Array<Item> {
        this.item = slots[0]
        return arrayOf(item)
    }

    override fun canAddItem(item: Item): Boolean {
        return false
    }

    override fun removeItem(vararg slots: Item): Array<Item> {
        item = Item.AIR
        return emptyArray()
    }

    override var contents: Map<Int, Item>
        get() = mapOf(0 to item)
        set(items) {
            item = items[0]!!
        }

    override fun sendContents(player: Player) {
    }

    override fun sendContents(vararg players: Player) {
    }

    override fun sendContents(players: Collection<Player>) {
    }

    override fun sendSlot(index: Int, player: Player) {
    }

    override fun sendSlot(index: Int, vararg players: Player) {
    }

    override fun sendSlot(index: Int, players: Collection<Player>) {
    }

    override fun getFreeSpace(item: Item): Int {
        return 0
    }

    override fun contains(item: Item): Boolean {
        return false
    }

    override fun all(item: Item): Map<Int, Item> {
        return emptyMap()
    }

    override fun first(item: Item, exact: Boolean): Int {
        return 0
    }

    override fun firstEmpty(item: Item?): Int {
        return 0
    }

    override fun decreaseCount(slot: Int) {
    }

    override fun remove(item: Item) {
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        item = Item.AIR
        return true
    }

    override fun clearAll() {
        item = Item.AIR
    }

    override val isFull: Boolean
        get() = false

    override val isEmpty: Boolean
        get() = false

    override val viewers: MutableSet<Player>
        get() = emptySet<Player>().toMutableSet()

    override val type: InventoryType
        get() = InventoryType.NONE

    override val holder: InventoryHolder
        get() = player

    override fun onOpen(who: Player) {
    }

    override fun open(who: Player): Boolean {
        return false
    }

    override fun onClose(who: Player) {
    }

    override fun close(who: Player) {
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
    }

    override fun addListener(listener: InventoryListener) {
    }

    override fun removeListener(listener: InventoryListener) {
    }
}
