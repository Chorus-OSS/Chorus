package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.ControlScheme
import org.chorus_oss.protocol.types.Vector2f
import org.chorus_oss.protocol.types.Vector3f
import org.chorus_oss.protocol.types.camera.CameraAimAssistTargetMode
import org.chorus_oss.protocol.types.camera.CameraAudioListener
import org.chorus_oss.protocol.types.camera.preset.CameraPreset
import org.chorus_oss.protocol.types.camera.preset.CameraPresetAimAssist

operator fun CameraPreset.Companion.invoke(from: org.chorus_oss.chorus.camera.data.CameraPreset): CameraPreset {
    return CameraPreset(
        name = from.identifier,
        parent = from.inheritFrom,
        posX = from.pos?.x,
        posY = from.pos?.y,
        posZ = from.pos?.z,
        rotX = from.yaw,
        rotY = from.pitch,
        rotSpeed = from.rotationSpeed,
        snapToTarget = from.snapToTarget.get(),
        horizontalRotationLimit = from.horizontalRotationLimit?.let(Vector2f::invoke),
        verticalRotationLimit = from.verticalRotationLimit?.let(Vector2f::invoke),
        continueTargeting = from.continueTargeting.get(),
        trackingRadius = from.blockListeningRadius.get(),
        viewOffset = from.viewOffset?.let(Vector2f::invoke),
        entityOffset = from.entityOffset?.let(Vector3f::invoke),
        radius = from.radius,
        minYawLimit = from.yawLimitMin,
        maxYawLimit = from.yawLimitMax,
        audioListener = from.listener?.let { CameraAudioListener.entries[it.ordinal] },
        playerEffects = from.playEffect.get(),
        aimAssist = from.aimAssist.get()?.let(CameraPresetAimAssist::invoke),
        controlScheme = from.controlScheme?.let { ControlScheme.entries[it.ordinal] },
    )
}

operator fun CameraPresetAimAssist.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.camera.aimassist.CameraPresetAimAssist): CameraPresetAimAssist {
    return CameraPresetAimAssist(
        presetId = from.presetId.get(),
        targetMode = from.targetMode.get()?.let { CameraAimAssistTargetMode.entries[it.ordinal] },
        angle = from.angle.get()?.let(Vector2f::invoke),
        distance = from.distance.get(),
    )
}