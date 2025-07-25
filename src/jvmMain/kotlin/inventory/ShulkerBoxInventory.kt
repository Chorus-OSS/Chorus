package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockUndyedShulkerBox
import org.chorus_oss.chorus.blockentity.BlockEntityShulkerBox
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos

class ShulkerBoxInventory(box: BlockEntityShulkerBox) : ContainerInventory(box, InventoryType.CONTAINER, 27) {
    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.SHULKER_BOX
        }
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)

        if (viewers.size == 1) {
            val pk = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(holder.vector3),
                eventType = 1,
                eventValue = 2,
            )

            val level = holder.level
            if (level != null) {
                level.addSound(holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_SHULKERBOXOPEN)
                level.addChunkPacket(
                    holder.vector3.x.toInt() shr 4,
                    holder.vector3.z.toInt() shr 4, pk
                )
            }
        }
    }

    override fun onClose(who: Player) {
        if (viewers.size == 1) {
            val pk = org.chorus_oss.protocol.packets.BlockEventPacket(
                blockPosition = BlockPos(holder.vector3),
                eventType = 1,
                eventValue = 0,
            )

            val level = holder.level
            if (level != null) {
                level.addSound(holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_SHULKERBOXCLOSED)
                level.addChunkPacket(
                    holder.vector3.x.toInt() shr 4,
                    holder.vector3.z.toInt() shr 4, pk
                )
            }
        }

        super.onClose(who)
    }

    override fun canAddItem(item: Item): Boolean {
        if (item.isBlock() && item.getSafeBlockState().toBlock() is BlockUndyedShulkerBox) {
            // Do not allow nested shulker boxes.
            return false
        }
        return super.canAddItem(item)
    }

    override fun canCauseVibration(): Boolean {
        return true
    }
}
