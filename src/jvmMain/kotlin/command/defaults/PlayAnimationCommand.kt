package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity

class PlayAnimationCommand(name: String) : VanillaCommand(name, "commands.playanimation.description") {
    init {
        this.permission = "chorus.command.playanimation"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("entity", CommandParamType.TARGET),
            CommandParameter.Companion.newType("animation", CommandParamType.STRING),
            CommandParameter.Companion.newType("next_state", true, CommandParamType.STRING),
            CommandParameter.Companion.newType("blend_out_time", true, CommandParamType.FLOAT),
            CommandParameter.Companion.newType("stop_expression", true, CommandParamType.STRING),
            CommandParameter.Companion.newType("controller", true, CommandParamType.STRING),
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
        val entities = list.getResult<List<Entity>>(0)!!
        if (entities.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        val packet = org.chorus_oss.protocol.packets.AnimateEntityPacket(
            animation = list.getResult(1)!!,
            nextState = when (list.hasResult(2)) {
                true -> list.getResult(2)!!
                else -> "default"
            },
            stopExpression = when (list.hasResult(2)) {
                true -> list.getResult(2)!!
                else -> "query.any_animation_finished"
            },
            stopExpressionVersion = 16777216,
            controller = when (list.hasResult(2)) {
                true -> list.getResult(2)!!
                else -> "__runtime_controller"
            },
            blendOutTime = when (list.hasResult(2)) {
                true -> list.getResult(2)!!
                else -> 0.0f
            },
            runtimeIDs = entities.map { it.getRuntimeID().toULong() }
        )
        // send animation
        Entity.playAnimationOnEntities(packet, entities)
        log.addSuccess("commands.playanimation.success").output()
        return 1
    }
}
