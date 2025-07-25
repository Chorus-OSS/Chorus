package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntitySculkCatalyst(level: Level, nbt: CompoundTag) : BlockEntity(level, nbt) {
    override val isBlockEntityValid: Boolean
        get() = levelBlock.id === BlockID.SCULK_CATALYST
}
