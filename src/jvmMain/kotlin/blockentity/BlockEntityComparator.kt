package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockRedstoneComparator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag


class BlockEntityComparator(chunk: IChunk, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
    @JvmField
    var outputSignal: Int = 0

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("OutputSignal")) {
            namedTag.putInt("OutputSignal", 0)
        }

        this.outputSignal = namedTag.getInt("OutputSignal")
    }

    override val isBlockEntityValid: Boolean
        get() = this.levelBlock is BlockRedstoneComparator

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putInt("OutputSignal", this.outputSignal)
    }
}
