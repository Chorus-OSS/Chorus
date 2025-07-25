package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.DispenserInventory
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityDispenser(chunk: IChunk, nbt: CompoundTag) : BlockEntityEjectable(chunk, nbt) {
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
