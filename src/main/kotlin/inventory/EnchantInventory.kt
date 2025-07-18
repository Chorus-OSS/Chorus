package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityEnchantTable
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.event.player.PlayerEnchantOptionsRequestEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.EnchantmentHelper
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType


class EnchantInventory(table: BlockEntityEnchantTable) : ContainerInventory(table, InventoryType.ENCHANTMENT, 2),
    BlockEntityInventoryNameable, CraftTypeInventory, SoleInventory {
    override fun networkSlotMap(): BiMap<Int, Int> {
        val map = super.networkSlotMap()
        map[0] = 14 //INPUT
        map[1] = 15 //MATERIAL
        return map
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.ENCHANTING_INPUT
        map[1] = ContainerSlotType.ENCHANTING_MATERIAL
        return map
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        if (index == 0) {
            if (before.isNothing) {
                for (viewer in this.viewers) {
                    val options = EnchantmentHelper.getEnchantOptions(
                        (holder as BlockEntityEnchantTable),
                        first, viewer.enchSeed
                    )

                    val event = PlayerEnchantOptionsRequestEvent(viewer, this, options)
                    if (!event.cancelled && !event.options.isEmpty()) {
                        val pk = org.chorus_oss.protocol.packets.PlayerEnchantOptionsPacket(
                            options = event.options.map { it.data }
                        )
                        viewer.sendPacket(pk)
                    }
                }
            } else {
                for (viewer in this.viewers) {
                    val pk = org.chorus_oss.protocol.packets.PlayerEnchantOptionsPacket(
                        options = emptyList(),
                    )
                    viewer.sendPacket(pk)
                }
            }
        }
        super.onSlotChange(index, before, false)
    }

    override fun onClose(who: Player) {
        super.onClose(who)
        var drops = arrayOf(getItem(0), getItem(1))
        drops = who.inventory.addItem(*drops)
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                (holder as BlockEntityEnchantTable).level.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }

        clear(0)
        clear(1)
    }

    val first: Item
        get() = this.getItem(0)

    val second: Item
        get() = this.getItem(1)

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder as BlockEntityEnchantTable
}
