package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.item.ItemStack


class PlayerCursorInventory(player: Player) : BaseInventory(player, InventoryType.INVENTORY, 1) {
    val item: Item
        get() = getItem(0)

    override fun init() {
        val slotTypeMap = super.slotTypeMap()
        slotTypeMap[0] = ContainerSlotType.CURSOR
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        for (player in players) {
            val id = SpecialWindowId.CURSOR.id
            val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                windowID = id.toUInt(),
                slot = index.toUInt(),
                container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                    org.chorus_oss.protocol.types.itemstack.ContainerSlotType.Cursor,
                    id,
                ),
                storageItem = ItemStack(Item.AIR),
                newItem = ItemStack(this.getUnclonedItem(index))
            )
            player.sendPacket(packet)
        }
    }

    override fun sendContents(vararg players: Player) {
        val id = SpecialWindowId.CURSOR.id
        val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
            windowID = id.toUInt(),
            slot = 0u,
            container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                org.chorus_oss.protocol.types.itemstack.ContainerSlotType.Cursor,
                id,
            ),
            storageItem = ItemStack(Item.AIR),
            newItem = ItemStack(this.getUnclonedItem(0))
        )
        Server.broadcastPacket(players.toList(), packet)
    }
}
