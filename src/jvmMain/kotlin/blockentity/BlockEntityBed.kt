package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor

class BlockEntityBed(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    var color: Int = 0

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("color")) {
            namedTag.putByte("color", 0)
        }

        this.color = namedTag.getByte("color").toInt()
    }

    override val isBlockEntityValid: Boolean
        get() = level.getBlockIdAt(
            position.floorX,
            position.floorY,
            position.floorZ
        ) === BlockID.BED

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putByte("color", this.color)
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound.putByte("color", this.color)

    val dyeColor: DyeColor
        get() = DyeColor.getByWoolData(color)
}
