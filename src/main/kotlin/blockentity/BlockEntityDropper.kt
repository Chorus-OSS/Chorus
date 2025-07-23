package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.DropperInventory
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityDropper(level: Level, nbt: CompoundTag) : BlockEntityEjectable(level, nbt) {
    override fun createInventory(): DropperInventory {
        inventory = DropperInventory(this)
        return getInventory()
    }

    override val blockEntityName: String
        get() = BlockEntityID.DROPPER

    fun getInventory(): DropperInventory {
        return inventory as DropperInventory
    }

    override val isBlockEntityValid: Boolean
        get() = this.levelBlock.id === BlockID.DROPPER
}
