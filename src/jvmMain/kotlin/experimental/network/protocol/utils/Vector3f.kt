package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.types.Vector3f

operator fun Vector3f.Companion.invoke(vec: Vector3): Vector3f {
    return Vector3f(
        vec.x.toFloat(),
        vec.y.toFloat(),
        vec.z.toFloat()
    )
}

operator fun Vector3f.Companion.invoke(vec: org.chorus_oss.chorus.math.Vector3f): Vector3f {
    return Vector3f(
        vec.x,
        vec.y,
        vec.z
    )
}

operator fun Vector3.Companion.invoke(from: Vector3f): Vector3 {
    return Vector3(
        from.x.toDouble(),
        from.y.toDouble(),
        from.z.toDouble()
    )
}