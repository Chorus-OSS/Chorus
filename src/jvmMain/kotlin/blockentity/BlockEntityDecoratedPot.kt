package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityDecoratedPot(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.DECORATED_POT

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putList("sherds", namedTag.getList("sherds"))
}
