package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

@JvmRecord
data class SelectionBox(
    val originX: Double, val originY: Double, val originZ: Double, val sizeX: Double, val sizeY: Double,
    val sizeZ: Double
) : NBTData {
    override fun toCompoundTag(): CompoundTag {
        return CompoundTag()
            .putBoolean("enabled", true)
            .putList(
                "origin", ListTag<FloatTag>()
                    .add(FloatTag(originX.toFloat()))
                    .add(FloatTag(originY.toFloat()))
                    .add(FloatTag(originZ.toFloat()))
            )
            .putList(
                "size", ListTag<FloatTag>()
                    .add(FloatTag(sizeX.toFloat()))
                    .add(FloatTag(sizeY.toFloat()))
                    .add(FloatTag(sizeZ.toFloat()))
            )
    }
}