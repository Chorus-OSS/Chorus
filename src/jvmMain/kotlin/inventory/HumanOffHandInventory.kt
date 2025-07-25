package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.IHuman
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.packets.InventoryContentPacket
import org.chorus_oss.protocol.types.inventory.FullContainerName
import org.chorus_oss.protocol.types.item.ItemStack

class HumanOffHandInventory(holder: IHuman) : BaseInventory(holder, InventoryType.INVENTORY, 1) {
    override fun init() {
        val map = super.networkSlotMap()
        map[0] = 1

        val map2 = super.slotTypeMap()
        map2[0] = ContainerSlotType.OFFHAND
    }

    fun setItem(item: Item) {
        setItem(0, item)
    }

    fun setItem(item: Item, send: Boolean) {
        setItem(0, item, send)
    }

    override fun setItem(index: Int, item: Item): Boolean {
        return super.setItem(0, item)
    }

    override fun setItem(index: Int, item: Item, send: Boolean): Boolean {
        return super.setItem(0, item, send)
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        val holder = this.holder
        if (holder is Player) {
            if (!holder.spawned) return
            if (send) {
                this.sendContents(this.viewers)
                this.sendContents(holder.getEntity().viewers.values)
            }
        }
    }

    override fun sendContents(vararg players: Player) {
        val item = this.getItem(0)
        val pk = this.createMobEquipmentPacket(item)

        for (player in players) {
            if (player === this.holder) {
                val packet = InventoryContentPacket(
                    windowID = SpecialWindowId.OFFHAND.id.toUInt(),
                    content = listOf(item).map { ItemStack(it) },
                    container = FullContainerName(
                        org.chorus_oss.protocol.types.itemstack.ContainerSlotType.Offhand,
                        null
                    ),
                    storageItem = ItemStack(Item.AIR)
                )

                player.sendPacket(packet)
                player.sendPacket(pk)
            } else {
                player.sendPacket(pk)
            }
        }
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        sendContents(*players)
    }

    private fun createMobEquipmentPacket(item: Item): org.chorus_oss.protocol.packets.MobEquipmentPacket {
        val pk = org.chorus_oss.protocol.packets.MobEquipmentPacket(
            entityRuntimeID = (holder as IHuman).getEntity().getRuntimeID().toULong(),
            newItem = ItemStack(item),
            inventorySlot = 1,
            hotbarSlot = 0,
            windowID = SpecialWindowId.OFFHAND.id.toByte()
        )
        return pk
    }
}
