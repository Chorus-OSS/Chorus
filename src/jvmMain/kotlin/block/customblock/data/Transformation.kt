package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

/**
 * supports rotation, scaling, and translation. The component can be added to the whole block and/or to individual block permutations. Transformed geometries still have the same restrictions that non-transformed geometries have such as a maximum size of 30/16 units.
 */
@JvmRecord
data class Transformation(val translation: Vector3, val scale: Vector3, val rotation: Vector3) :
    NBTData {
    override fun toCompoundTag(): CompoundTag {
        val rx = (rotation.floorX % 360) / 90
        val ry = (rotation.floorY % 360) / 90
        val rz = (rotation.floorZ % 360) / 90
        return CompoundTag()
            .putInt("RX", rx)
            .putInt("RY", ry)
            .putInt("RZ", rz)
            .putFloat("SX", scale.x.toFloat())
            .putFloat("SY", scale.y.toFloat())
            .putFloat("SZ", scale.z.toFloat())
            .putFloat("TX", translation.x.toFloat())
            .putFloat("TY", translation.y.toFloat())
            .putFloat("TZ", translation.z.toFloat())
    }
}
