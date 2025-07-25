package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityFlowerPot(level: Level, nbt: CompoundTag) : BlockEntitySpawnable(level, nbt) {
    init {
        isMovable = true
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = block.id
            return blockId == BlockID.FLOWER_POT
        }

    override val spawnCompound: CompoundTag
        get() {
            val tag = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
            if (namedTag.containsCompound("PlantBlock")) tag.putCompound(
                "PlantBlock",
                namedTag.getCompound("PlantBlock")
            )
            return tag
        }
}
