package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.ExecutorCommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PositionNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Transform

class ExecuteCommandOld(name: String) : VanillaCommand(name, "old execute command", "commands.execute.usage") {
    init {
        this.permission = "chorus.command.executeold"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("origin", CommandParamType.TARGET),
            CommandParameter.Companion.newType("position", CommandParamType.POSITION),
            CommandParameter.Companion.newType("command", CommandParamType.COMMAND)
        )
        commandParameters["detect"] = arrayOf(
            CommandParameter.Companion.newType("origin", CommandParamType.TARGET),
            CommandParameter.Companion.newType("position", CommandParamType.POSITION),
            CommandParameter.Companion.newEnum("detect", arrayOf("detect")),
            CommandParameter.Companion.newType("detectPos", CommandParamType.POSITION),
            CommandParameter.Companion.newType("block", CommandParamType.INT),
            CommandParameter.Companion.newType("data", CommandParamType.INT),
            CommandParameter.Companion.newType("command", CommandParamType.COMMAND)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        var num = 0
        val list = result.value
        val entities = list.getResult<List<Entity>>(0)!!
        if (entities.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        when (result.key) {
            "default" -> {
                val command = list.getResult<String>(2)!!
                for (entity in entities) {
                    val pos = (list[1] as PositionNode).get<Locator>(entity.locator)
                    val executeSender = ExecutorCommandSender(
                        sender, entity, Transform.fromObject(
                            pos!!.position, pos.level
                        )
                    )
                    val n = Server.instance.executeCommand(executeSender, command)
                    if (n == 0) {
                        log.addError("commands.execute.failed", command, entity.getEntityName())
                    } else num += n
                }
            }

            "detect" -> {
                val blockId = list.getResult<String>(4)
                val meta = list.getResult<Int>(5)!!
                val command = list.getResult<String>(6)!!
                for (entity in entities) {
                    val pos = (list[1] as PositionNode).get<Locator>(entity.locator)
                    val detect = (list[3] as PositionNode).get<Locator>(
                        pos!!
                    )
                    if (detect!!.levelBlock.id === blockId && detect!!.levelBlock.blockState.specialValue()
                            .toInt() == meta
                    ) {
                        val executeSender = ExecutorCommandSender(
                            sender, entity, Transform.fromObject(
                                pos.position, pos.level
                            )
                        )
                        val n = Server.instance.executeCommand(executeSender, command)
                        if (n == 0) {
                            log.addError("commands.execute.failed", command, entity.getEntityName())
                        } else num += n
                    }
                }
            }
        }
        log.output()
        return num
    }
}
