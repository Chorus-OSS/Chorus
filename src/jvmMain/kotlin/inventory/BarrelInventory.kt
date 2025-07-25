package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockBarrel
import org.chorus_oss.chorus.blockentity.BlockEntityBarrel
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class BarrelInventory(barrel: BlockEntityBarrel) : ContainerInventory(barrel, InventoryType.CONTAINER, 27),
    BlockEntityInventoryNameable {
    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.BARREL
        }
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)

        if (viewers.size == 1) {
            val barrel = this.holder as BlockEntityBarrel
            val level = barrel.level
            val block = barrel.block
            if (block is BlockBarrel) {
                if (!block.isOpen) {
                    block.isOpen = true
                    level.setBlock(block.position, block, direct = true, update = true)
                    level.addSound(block.position, Sound.BLOCK_BARREL_OPEN)
                }
            }
        }
    }

    override fun onClose(who: Player) {
        super.onClose(who)

        if (viewers.isEmpty()) {
            val barrel = this.holder as BlockEntityBarrel
            val level = barrel.level
            val block = barrel.block
            if (block is BlockBarrel) {
                if (block.isOpen) {
                    block.isOpen = false
                    level.setBlock(block.position, block, direct = true, update = true)
                    level.addSound(block.position, Sound.BLOCK_BARREL_CLOSE)
                }
            }
        }
    }

    override fun canCauseVibration(): Boolean {
        return true
    }

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder as BlockEntityNameable
}
