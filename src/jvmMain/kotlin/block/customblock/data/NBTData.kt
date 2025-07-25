package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag

interface NBTData {
    fun toCompoundTag(): CompoundTag
}
