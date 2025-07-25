package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.blockentity.BlockEntityCommandBlock
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.event.inventory.InventoryOpenEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType

// Implement the command block's ui
class CommandBlockInventory(override val holder: BlockEntityCommandBlock) : Inventory,
    BlockEntityInventoryNameable {
    override val viewers: MutableSet<Player> = HashSet()
    private var listeners: MutableList<InventoryListener>? = null

    override val size: Int
        get() = 0

    override var maxStackSize: Int
        get() = 0
        set(size) {
        }

    override fun getItem(index: Int): Item {
        return Item.AIR
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        return false
    }

    override fun addItem(vararg slots: Item): Array<Item> {
        return emptyArray()
    }

    override fun canAddItem(item: Item): Boolean {
        return false
    }

    override fun removeItem(vararg slots: Item): Array<Item> {
        return emptyArray()
    }

    override var contents: Map<Int, Item>
        get() = emptyMap()
        set(items) {
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
        return false
    }

    override fun clearAll() {
    }

    override val isFull: Boolean
        get() = true

    override val isEmpty: Boolean
        get() = true

    override val type: InventoryType
        get() = InventoryType.COMMAND_BLOCK

    override fun onOpen(who: Player) {
        if (who.isOp && who.isCreative) {
            viewers.add(who)
            who.sendPacket(
                org.chorus_oss.protocol.packets.ContainerOpenPacket(
                    containerID = who.getWindowId(this).toByte(),
                    containerType = ContainerType(type),
                    position = BlockPos(holder.vector3),
                    targetActorID = who.getUniqueID()
                )
            )
        }
    }

    override fun open(who: Player): Boolean {
        if (who.getWindowId(this) != -1) { //todo hack, ContainerClosePacket no longer triggers for command block and struct block, finding the correct way to close them
            who.removeWindow(this)
        }

        val ev = InventoryOpenEvent(this, who)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }
        this.onOpen(who)

        return true
    }

    override fun close(who: Player) {
        this.onClose(who)
    }

    override fun onClose(who: Player) {
        val containerId = who.getWindowId(this)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerClosePacket(
                containerID = containerId.toByte(),
                containerType = ContainerType(type),
                serverInitiatedClose = who.closingWindowId != containerId
            )
        )
        viewers.remove(who)
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        if (this.listeners != null) {
            for (listener in listeners!!) {
                listener.onInventoryChanged(this, before, index)
            }
        }
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

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder
}
