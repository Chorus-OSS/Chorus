package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockTrappedChest
import org.chorus_oss.chorus.blockentity.BlockEntityChest
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.event.redstone.RedstoneUpdateEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.chorus.utils.LevelException
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.protocol.types.BlockPos

class ChestInventory(chest: BlockEntityChest) : ContainerInventory(chest, InventoryType.CONTAINER, 27),
    BlockEntityInventoryNameable {
    var doubleInventory: DoubleChestInventory? = null

    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.LEVEL_ENTITY
        }
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        for (i in 0..<this.size) {
            map[i] = ContainerSlotType.INVENTORY
        }
        return map
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
                level.addSound((holder as BlockEntityChest).position.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTOPEN)
                level.addChunkPacket(
                    holder.vector3.x.toInt() shr 4,
                    holder.vector3.z.toInt() shr 4, pk
                )
            }
        }
        try {
            if ((holder as BlockEntityChest).block is BlockTrappedChest) {
                val event = RedstoneUpdateEvent((holder as BlockEntityChest).block)
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    RedstoneComponent.updateAllAroundRedstone(this.holder as BlockEntityChest)
                }
            }
        } catch (ignored: LevelException) {
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
                level.addSound(holder.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_CHESTCLOSED)
                level.addChunkPacket(
                    holder.vector3.x.toInt() shr 4,
                    holder.vector3.z.toInt() shr 4, pk
                )
            }
        }

        try {
            if ((holder as BlockEntityChest).block is BlockTrappedChest) {
                val event = RedstoneUpdateEvent((holder as BlockEntityChest).block)
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    RedstoneComponent.updateAllAroundRedstone((holder as BlockEntityChest))
                }
            }
        } catch (ignored: LevelException) {
        }
        super.onClose(who)
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        if (this.doubleInventory != null) {
            doubleInventory!!.sendSlot(this, index, *players)
        } else {
            super.sendSlot(index, *players)
        }
    }

    override fun canCauseVibration(): Boolean {
        return true
    }

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = (holder as BlockEntityChest)
}
