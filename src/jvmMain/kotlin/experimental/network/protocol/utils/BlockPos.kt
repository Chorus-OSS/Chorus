package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.types.BlockPos
import kotlin.math.floor

operator fun BlockPos.Companion.invoke(vec: Vector3): BlockPos {
    return BlockPos(
        floor(vec.x).toInt(),
        floor(vec.y).toInt(),
        floor(vec.z).toInt(),
    )
}

operator fun Vector3.Companion.invoke(pos: BlockPos): Vector3 {
    return Vector3(
        pos.x.toDouble(),
        pos.y.toDouble(),
        pos.z.toDouble(),
    )
}