package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.HumanInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.enchantment.Enchantment.Companion.getEnchantments
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.MobEquipmentPacket


class MobEquipmentProcessor : PacketProcessor<MobEquipmentPacket> {
    override fun handle(player: Player, packet: MobEquipmentPacket) {

        if (!player.spawned || !player.isAlive()) {
            return
        }

        if (packet.hotbarSlot < 0 || packet.hotbarSlot > 8) {
            player.close("§cPacket handling error")
            return
        }

        val newItem = Item(packet.newItem.item)

        if (!newItem.isNothing) {
            if (newItem.enchantments.size > getEnchantments().size) { // Last Enchant Id
                player.close("§cPacket handling error")
                return
            }
            if (newItem.lore.size > 100) {
                player.close("§cPacket handling error")
                return
            }
            if (newItem.canPlaceOn.size() > 250) {
                player.close("§cPacket handling error")
                return
            }
            if (newItem.canDestroy.size() > 250) {
                player.close("§cPacket handling error")
                return
            }
        }

        val inv = player.getWindowById(packet.windowID.toInt())

        if (inv == null) {
            log.debug(
                "Player {} has no open container with window ID {}",
                player.getEntityName(),
                packet.windowID
            )
            return
        }

        if (inv is HumanInventory && inv.heldItemIndex == packet.hotbarSlot.toInt()) {
            return
        }

        val item = inv.getItem(packet.hotbarSlot.toInt())

        if (!item.equals(newItem, checkDamage = false, checkCompound = true)) {
            val fixItem = get(item.id, item.damage, item.getCount(), item.compoundTag)
            if (fixItem.equals(newItem, checkDamage = false, checkCompound = true)) {
                inv.setItem(packet.hotbarSlot.toInt(), fixItem)
            } else {
                log.debug("Tried to equip {} but have {} in target slot", newItem, fixItem)
                inv.sendContents(player)
            }
        }

        if (inv is HumanInventory) {
            inv.equipItem(packet.hotbarSlot.toInt())
        }

        player.setDataFlag(EntityFlag.USING_ITEM, false)
    }

    override val packetID: Int = MobEquipmentPacket.id

    companion object : Loggable
}
