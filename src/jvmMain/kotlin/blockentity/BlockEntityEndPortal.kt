package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityEndPortal(level: Level, nbt: CompoundTag) : BlockEntitySpawnable(level, nbt) {
    override val isBlockEntityValid: Boolean
        get() = level
            .getBlockIdAt(floorX, floorY, floorZ) === BlockID.END_PORTAL
}
