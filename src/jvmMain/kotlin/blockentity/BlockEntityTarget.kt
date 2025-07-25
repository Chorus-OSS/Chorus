package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityTarget(level: Level, nbt: CompoundTag) : BlockEntity(level, nbt) {
    override val isBlockEntityValid: Boolean
        get() = levelBlock.id === BlockID.TARGET

    var activePower: Int
        get() = namedTag.getInt("activePower").coerceIn(0, 15)
        set(power) {
            namedTag.putInt("activePower", power)
        }
}
