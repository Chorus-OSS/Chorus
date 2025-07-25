package org.chorus_oss.chorus.level.vibration

import org.chorus_oss.chorus.math.Vector3

/**
 * @param initiator The object which cause the vibration (can be an instance of Block, Entity ...)
 * @param source    The vibration source pos
 * @param type      Vibration type
 */
@JvmRecord
data class VibrationEvent(val initiator: Any?, val source: Vector3, val type: VibrationType)
