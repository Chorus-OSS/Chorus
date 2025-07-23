package org.chorus_oss.chorus.camera.data

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.math.Vector2f
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.network.protocol.types.ControlScheme
import org.chorus_oss.chorus.network.protocol.types.camera.CameraAudioListener
import org.chorus_oss.chorus.network.protocol.types.camera.aimassist.CameraPresetAimAssist


import java.util.*
import java.util.function.Consumer

class CameraPreset @JvmOverloads constructor(
    val identifier: String,
    val inheritFrom: String = "",
    val pos: Vector3f? = null,
    val yaw: Float? = null,
    val pitch: Float? = null,
    val rotationSpeed: Float? = null,
    val snapToTarget: Boolean? = null,
    val blockListeningRadius: Float? = null,
    val viewOffset: Vector2f? = null,
    val entityOffset: Vector3f? = null,
    val radius: Float? = null,
    val yawLimitMin: Float? = null,
    val yawLimitMax: Float? = null,
    val listener: CameraAudioListener? = null,
    val playEffect: Boolean? = null,
    val horizontalRotationLimit: Vector2f? = null,
    val verticalRotationLimit: Vector2f? = null,
    val continueTargeting: Boolean? = null,
    val aimAssist: CameraPresetAimAssist? = null,
    val controlScheme: ControlScheme? = null,
) {
    private var id = 0

    fun getId() = id

    companion object {
        private val PRESETS: MutableMap<String, CameraPreset> = TreeMap()

        @JvmStatic
        val presets: Map<String, CameraPreset>
            get() = PRESETS

        @JvmStatic
        fun getPreset(identifier: String): CameraPreset? {
            return presets[identifier]
        }

        fun registerCameraPresets(vararg presets: CameraPreset) {
            for (preset in presets) {
                require(!PRESETS.containsKey(preset.identifier)) { "Camera preset " + preset.identifier + " already exists!" }
                PRESETS[preset.identifier] = preset
                CommandEnum.CAMERA_PRESETS.updateSoftEnum(
                    org.chorus_oss.protocol.packets.UpdateSoftEnumPacket.Companion.ActionType.Add,
                    preset.identifier
                )
            }
            var id = 0
            //重新分配id
            for (preset in presets) {
                preset.id = id++
            }
            Server.instance.onlinePlayers.values.forEach(Consumer { obj: Player -> obj.sendCameraPresets() })
        }

        val FIRST_PERSON: CameraPreset = CameraPreset(
            "minecraft:first_person"
        )
        val FREE: CameraPreset = CameraPreset(
            "minecraft:free",
            pos = Vector3f(0f, 0f, 0f),
            yaw = 0f,
            pitch = 0f,
        )
        val THIRD_PERSON: CameraPreset = CameraPreset(
            "minecraft:third_person"
        )
        val THIRD_PERSON_FRONT: CameraPreset = CameraPreset(
            "minecraft:third_person_front"
        )

        init {
            registerCameraPresets(FIRST_PERSON, FREE, THIRD_PERSON, THIRD_PERSON_FRONT)
        }
    }
}
