package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.camera.data.CameraPreset.Companion.getPreset
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.FloatNode
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.tree.node.RelativeFloatNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.protocol.types.camera.CameraEase
import org.chorus_oss.protocol.types.camera.instruction.CameraFadeInstruction
import org.chorus_oss.protocol.types.camera.instruction.CameraSetInstruction

/**
 * TODO: The multilingual text of this command does not seem to work properly
 */
class CameraCommand(name: String) : VanillaCommand(name, "commands.camera.description") {
    init {
        this.permission = "chorus.command.camera"
        commandParameters.clear()
        commandParameters["clear"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("clear", false, arrayOf("clear"))
        )
        commandParameters["fade"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("fade", false, arrayOf("fade"))
        )
        commandParameters["fade-color"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("fade", false, arrayOf("fade")),
            CommandParameter.Companion.newEnum("color", false, arrayOf("color")),
            CommandParameter.Companion.newType("red", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("green", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("blue", false, CommandParamType.FLOAT)
        )
        commandParameters["fade-time-color"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("fade", false, arrayOf("fade")),
            CommandParameter.Companion.newEnum("time", false, arrayOf("time")),
            CommandParameter.Companion.newType("fadeInSeconds", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("holdSeconds", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("fadeOutSeconds", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newEnum("color", false, arrayOf("color")),
            CommandParameter.Companion.newType("red", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("green", false, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("blue", false, CommandParamType.FLOAT)
        )
        commandParameters["set-default"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("default", true, arrayOf("default"))
        )
        commandParameters["set-rot"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("rot", false, arrayOf("rot")),
            CommandParameter.Companion.newType("xRot", false, CommandParamType.VALUE, RelativeFloatNode()),
            CommandParameter.Companion.newType("yRot", false, CommandParamType.VALUE, RelativeFloatNode())
        )
        commandParameters["set-pos"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("pos", false, arrayOf("pos")),
            CommandParameter.Companion.newType("position", false, CommandParamType.POSITION),
        )
        commandParameters["set-pos-rot"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("pos", false, arrayOf("pos")),
            CommandParameter.Companion.newType("position", false, CommandParamType.POSITION),
            CommandParameter.Companion.newEnum("rot", false, arrayOf("rot")),
            CommandParameter.Companion.newType("xRot", false, CommandParamType.VALUE, RelativeFloatNode()),
            CommandParameter.Companion.newType("yRot", false, CommandParamType.VALUE, RelativeFloatNode())
        )
        commandParameters["set-ease-default"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("ease", false, arrayOf("ease")),
            CommandParameter.Companion.newType(
                "easeTime",
                false,
                CommandParamType.FLOAT,
                FloatNode()
            ),
            CommandParameter.Companion.newEnum("easeType", false, EASE_MAP.keys.toTypedArray()),
            CommandParameter.Companion.newEnum("default", true, arrayOf("default"))
        )
        commandParameters["set-ease-rot"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("ease", false, arrayOf("ease")),
            CommandParameter.Companion.newType(
                "easeTime",
                false,
                CommandParamType.FLOAT,
                FloatNode()
            ),
            CommandParameter.Companion.newEnum("easeType", false, EASE_MAP.keys.toTypedArray()),
            CommandParameter.Companion.newEnum("rot", false, arrayOf("rot")),
            CommandParameter.Companion.newType("xRot", false, CommandParamType.VALUE, RelativeFloatNode()),
            CommandParameter.Companion.newType("yRot", false, CommandParamType.VALUE, RelativeFloatNode())
        )
        commandParameters["set-ease-pos"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("ease", false, arrayOf("ease")),
            CommandParameter.Companion.newType(
                "easeTime",
                false,
                CommandParamType.FLOAT,
                FloatNode()
            ),
            CommandParameter.Companion.newEnum("easeType", false, EASE_MAP.keys.toTypedArray()),
            CommandParameter.Companion.newEnum("pos", false, arrayOf("pos")),
            CommandParameter.Companion.newType("position", false, CommandParamType.POSITION),
        )
        commandParameters["set-ease-pos-rot"] = arrayOf(
            CommandParameter.Companion.newType("players", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("set", false, arrayOf("set")),
            CommandParameter.Companion.newEnum("preset", false, CommandEnum.Companion.CAMERA_PRESETS),
            CommandParameter.Companion.newEnum("ease", false, arrayOf("ease")),
            CommandParameter.Companion.newType(
                "easeTime",
                false,
                CommandParamType.FLOAT,
                FloatNode()
            ),
            CommandParameter.Companion.newEnum("easeType", false, EASE_MAP.keys.toTypedArray()),
            CommandParameter.Companion.newEnum("pos", false, arrayOf("pos")),
            CommandParameter.Companion.newType("position", false, CommandParamType.POSITION),
            CommandParameter.Companion.newEnum("rot", false, arrayOf("rot")),
            CommandParameter.Companion.newType("xRot", false, CommandParamType.VALUE, RelativeFloatNode()),
            CommandParameter.Companion.newType("yRot", false, CommandParamType.VALUE, RelativeFloatNode())
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val list = result.value
        val players = list.getResult<List<Player>>(0)!!
        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        val playerNames = players.map { it.getEntityName() }.reduceOrNull { a, b -> "$a $b" } ?: ""

        val senderLocation = sender.transform

        var set: CameraSetInstruction? = null
        var clear: Boolean? = null
        var fade: CameraFadeInstruction? = null

        when (result.key) {
            "clear" -> clear = true

            "fade" -> fade = CameraFadeInstruction()

            "fade-color" -> fade = CameraFadeInstruction(
                color = org.chorus_oss.protocol.types.Color(
                    r = (list[3].get<Float>()!! * 255f).toInt().toByte(),
                    g = (list[4].get<Float>()!! * 255f).toInt().toByte(),
                    b = (list[5].get<Float>()!! * 255f).toInt().toByte(),
                )
            )

            "fade-time-color" -> fade = CameraFadeInstruction(
                timeData = CameraFadeInstruction.Companion.TimeData(
                    fadeInDuration = list[3].get()!!,
                    waitDuration = list[4].get()!!,
                    fadeOutDuration = list[5].get()!!
                ),
                color = org.chorus_oss.protocol.types.Color(
                    r = (list[7].get<Float>()!! * 255f).toInt().toByte(),
                    g = (list[8].get<Float>()!! * 255f).toInt().toByte(),
                    b = (list[9].get<Float>()!! * 255f).toInt().toByte(),
                )
            )

            "set-default" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                removeIgnoreStartingValuesComponent = false,
            )

            "set-rot" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                rotation = org.chorus_oss.protocol.types.Vector2f(
                    (list[4] as RelativeFloatNode).get(senderLocation.pitch.toFloat()),
                    (list[5] as RelativeFloatNode).get(senderLocation.yaw.toFloat())
                ),
                removeIgnoreStartingValuesComponent = false,
            )

            "set-pos" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                position = org.chorus_oss.protocol.types.Vector3f(list[4].get<Locator>()!!.vector3),
                removeIgnoreStartingValuesComponent = false,
            )

            "set-pos-rot" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                position = org.chorus_oss.protocol.types.Vector3f(list[4].get<Locator>()!!.vector3),
                rotation = org.chorus_oss.protocol.types.Vector2f(
                    (list[6] as RelativeFloatNode).get(senderLocation.pitch.toFloat()),
                    (list[7] as RelativeFloatNode).get(senderLocation.yaw.toFloat())
                ),
                removeIgnoreStartingValuesComponent = false
            )

            "set-ease-default" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                ease = CameraEase(
                    type = EASE_MAP.getValue(list[5].get<String>()!!),
                    duration = list[4].get<Float>()!!
                ),
                removeIgnoreStartingValuesComponent = false,
            )

            "set-ease-rot" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                ease = CameraEase(
                    type = EASE_MAP.getValue(list[5].get<String>()!!),
                    duration = list[4].get<Float>()!!
                ),
                rotation = org.chorus_oss.protocol.types.Vector2f(
                    (list[7] as RelativeFloatNode).get(senderLocation.pitch.toFloat()),
                    (list[8] as RelativeFloatNode).get(senderLocation.yaw.toFloat())
                ),
                removeIgnoreStartingValuesComponent = false,
            )

            "set-ease-pos" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                ease = CameraEase(
                    type = EASE_MAP.getValue(list[5].get<String>()!!),
                    duration = list[4].get<Float>()!!
                ),
                position = org.chorus_oss.protocol.types.Vector3f(list[7].get<Locator>()!!.vector3),
                removeIgnoreStartingValuesComponent = false,
            )

            "set-ease-pos-rot" -> set = CameraSetInstruction(
                preset = getPreset(list[2].get()!!)?.getId()?.toUInt() ?: run {
                    log.addError("commands.camera.invalid-preset").output()
                    return 0
                },
                ease = CameraEase(
                    type = EASE_MAP.getValue(list[5].get<String>()!!),
                    duration = list[4].get<Float>()!!
                ),
                position = org.chorus_oss.protocol.types.Vector3f(list[7].get<Locator>()!!.vector3),
                rotation = org.chorus_oss.protocol.types.Vector2f(
                    (list[9] as RelativeFloatNode).get(senderLocation.pitch.toFloat()),
                    (list[10] as RelativeFloatNode).get(senderLocation.yaw.toFloat())
                ),
                removeIgnoreStartingValuesComponent = false,
            )

            else -> return 0
        }

        val packet = org.chorus_oss.protocol.packets.CameraInstructionPacket(
            set = set,
            clear = clear,
            fade = fade,
            target = null,
            removeTarget = null
        )

        for (player in players) {
            player.sendPacket(packet)
        }
        log.addSuccess("commands.camera.success", playerNames).output()
        return 1
    }

    companion object {
        val EASE_MAP = mapOf(
            "linear" to CameraEase.Companion.Type.Linear,
            "spring" to CameraEase.Companion.Type.Spring,
            "in_quad" to CameraEase.Companion.Type.InQuad,
            "out_quad" to CameraEase.Companion.Type.OutQuad,
            "in_out_quad" to CameraEase.Companion.Type.InOutQuad,
            "in_cubic" to CameraEase.Companion.Type.InCubic,
            "out_cubic" to CameraEase.Companion.Type.OutCubic,
            "in_out_cubic" to CameraEase.Companion.Type.InOutCubic,
            "in_quart" to CameraEase.Companion.Type.InQuart,
            "out_quart" to CameraEase.Companion.Type.OutQuart,
            "in_out_quart" to CameraEase.Companion.Type.InOutQuart,
            "in_quint" to CameraEase.Companion.Type.InQuint,
            "out_quint" to CameraEase.Companion.Type.OutQuint,
            "in_out_quint" to CameraEase.Companion.Type.InOutQuint,
            "in_sine" to CameraEase.Companion.Type.InSine,
            "out_sine" to CameraEase.Companion.Type.OutSine,
            "in_out_sine" to CameraEase.Companion.Type.InOutSine,
            "in_expo" to CameraEase.Companion.Type.InExpo,
            "out_expo" to CameraEase.Companion.Type.OutExpo,
            "in_out_expo" to CameraEase.Companion.Type.InOutExpo,
            "in_circ" to CameraEase.Companion.Type.InCirc,
            "out_circ" to CameraEase.Companion.Type.OutCirc,
            "in_out_circ" to CameraEase.Companion.Type.InOutCirc,
            "in_bounce" to CameraEase.Companion.Type.InBounce,
            "out_bounce" to CameraEase.Companion.Type.OutBounce,
            "in_out_bounce" to CameraEase.Companion.Type.InOutBounce,
            "in_back" to CameraEase.Companion.Type.InBack,
            "out_back" to CameraEase.Companion.Type.OutBack,
            "in_out_back" to CameraEase.Companion.Type.InOutBack,
            "in_elastic" to CameraEase.Companion.Type.InElastic,
            "out_elastic" to CameraEase.Companion.Type.OutElastic,
            "in_out_elastic" to CameraEase.Companion.Type.InOutElastic,
        )
    }
}
