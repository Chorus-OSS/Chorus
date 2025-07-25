package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityChest
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.network.protocol.types.inventory.FullContainerName
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.item.ItemStack


class DoubleChestInventory(left: BlockEntityChest, right: BlockEntityChest) :
    ContainerInventory(left.realInventory.holder, InventoryType.CONTAINER, 27 + 27) {
    val leftSide: ChestInventory = left.realInventory
    val rightSide: ChestInventory

    override var contents: Map<Int, Item>
        get() {
            val contents: MutableMap<Int, Item> = HashMap()
            for (i in 0..<this.size) {
                contents[i] = getItem(i)
            }

            return contents
        }
        set(items) {
            val items1 = items.toMutableMap()
            if (items1.size > this.size) {
                items1.keys.removeIf { slot: Int -> slot >= this.size }
            }

            for (i in 0..<this.size) {
                val item = items1[i]
                var isSet = false

                if (item != null) {
                    isSet = if (i < leftSide.size) {
                        leftSide.setItem(i, item)
                    } else {
                        rightSide.setItem(i - leftSide.size, item)
                    }
                }

                if (!isSet) {
                    this.clear(i)
                }
            }
        }

    init {
        leftSide.doubleInventory = (this)

        this.rightSide = right.realInventory
        rightSide.doubleInventory = (this)

        val items: MutableMap<Int, Item> = HashMap()
        // First we add the items from the left chest
        for (idx in 0..<leftSide.size) {
            if (leftSide.contents.containsKey(idx)) { // Don't forget to skip empty slots!
                items[idx] = leftSide.contents[idx]!!
            }
        }
        // And them the items from the right chest
        for (idx in 0..<rightSide.size) {
            if (rightSide.contents.containsKey(idx)) { // Don't forget to skip empty slots!
                items[idx + leftSide.size] =
                    rightSide.contents[idx]!! // idx + this.left.size so we don't overlap left chest items
            }
        }

        this.contents = items
    }

    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.LEVEL_ENTITY
        }
    }

    override fun getItem(index: Int): Item {
        return if (index < leftSide.size) leftSide.getItem(index) else rightSide.getItem(index - rightSide.size)
    }

    override fun getUnclonedItem(index: Int): Item {
        return if (index < leftSide.size) leftSide.getUnclonedItem(index) else rightSide.getUnclonedItem(index - rightSide.size)
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        return if (index < leftSide.size) leftSide.setItem(
            index,
            item,
            send
        ) else rightSide.setItem(index - rightSide.size, item, send)
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        return if (index < leftSide.size) leftSide.clear(
            index,
            send
        ) else rightSide.clear(index - rightSide.size, send)
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)
        leftSide.viewers.add(who)
        rightSide.viewers.add(who)

        if (viewers.size == 1) {
            val pk1 = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(leftSide.holder.vector3),
                eventType = 1,
                eventValue = 2,
            )
            var level = leftSide.holder.level
            if (level != null) {
                level.addSound(leftSide.holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTOPEN)
                level.addChunkPacket(
                    leftSide.holder.vector3.x.toInt() shr 4,
                    leftSide.holder.vector3.z.toInt() shr 4, pk1
                )
            }

            val pk2 = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(rightSide.holder.vector3),
                eventType = 1,
                eventValue = 2,
            )

            level = rightSide.holder.level
            if (level != null) {
                level.addSound(rightSide.holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTOPEN)
                level.addChunkPacket(
                    rightSide.holder.vector3.x.toInt() shr 4,
                    rightSide.holder.vector3.z.toInt() shr 4, pk2
                )
            }
        }
    }

    override fun onClose(who: Player) {
        if (viewers.size == 1) {
            val pk1 = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(rightSide.holder.vector3),
                eventType = 1,
                eventValue = 0,
            )

            var level = rightSide.holder.level
            if (level != null) {
                level.addSound(rightSide.holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTCLOSED)
                level.addChunkPacket(
                    rightSide.holder.vector3.x.toInt() shr 4,
                    rightSide.holder.vector3.z.toInt() shr 4, pk1
                )
            }

            val pk2 = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(leftSide.holder.vector3),
                eventType = 1,
                eventValue = 0,
            )

            level = leftSide.holder.level
            if (level != null) {
                level.addSound(leftSide.holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTCLOSED)
                level.addChunkPacket(
                    leftSide.holder.vector3.x.toInt() shr 4,
                    leftSide.holder.vector3.z.toInt() shr 4, pk2
                )
            }
        }

        leftSide.viewers.remove(who)
        rightSide.viewers.remove(who)
        super.onClose(who)
    }

    fun sendSlot(inv: Inventory, index: Int, vararg players: Player) {
        val i = if (inv === this.rightSide) leftSide.size + index else index
        val slot = toNetworkSlot(i)
        for (player in players) {
            val id = player.getWindowId(this)
            if (id == -1) {
                this.close(player)
                continue
            }

            val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                windowID = id.toUInt(),
                slot = slot.toUInt(),
                container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                    FullContainerName(
                        this.getSlotType(slot),
                        id
                    )
                ),
                storageItem = ItemStack(Item.AIR),
                newItem = ItemStack(inv.getUnclonedItem(index))
            )
            player.sendPacket(packet)
        }
    }
}
