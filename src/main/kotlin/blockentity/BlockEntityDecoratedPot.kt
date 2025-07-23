package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityDecoratedPot(level: Level, nbt: CompoundTag) : BlockEntitySpawnable(level, nbt) {
    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.DECORATED_POT

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putList("sherds", namedTag.getList("sherds"))
}
