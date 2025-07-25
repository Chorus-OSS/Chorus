package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.math.Rotator2
import org.chorus_oss.protocol.types.Vector2f

operator fun Vector2f.Companion.invoke(value: Rotator2): Vector2f {
    return Vector2f(
        x = value.pitch.toFloat(),
        y = value.yaw.toFloat(),
    )
}

operator fun Vector2f.Companion.invoke(from: org.chorus_oss.chorus.math.Vector2f): Vector2f {
    return Vector2f(
        x = from.x,
        y = from.y,
    )
}