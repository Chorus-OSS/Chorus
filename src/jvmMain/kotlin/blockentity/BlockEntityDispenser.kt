package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.DispenserInventory
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityDispenser(level: Level, nbt: CompoundTag) : BlockEntityEjectable(level, nbt) {
    override fun createInventory(): DispenserInventory {
        inventory = DispenserInventory(this)
        return getInventory()
    }

    override val blockEntityName: String
        get() = BlockEntityID.DISPENSER

    fun getInventory(): DispenserInventory {
        return inventory as DispenserInventory
    }

    override val isBlockEntityValid: Boolean
        get() = this.levelBlock.id === BlockID.DISPENSER
}
