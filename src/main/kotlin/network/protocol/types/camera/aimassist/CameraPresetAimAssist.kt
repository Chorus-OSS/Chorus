package org.chorus_oss.chorus.network.protocol.types.camera.aimassist

import org.chorus_oss.chorus.math.Vector2f

data class CameraPresetAimAssist(
    var presetId: String?,
    var targetMode: CameraAimAssist?,
    val angle: Vector2f?,
    val distance: Float?,
)
