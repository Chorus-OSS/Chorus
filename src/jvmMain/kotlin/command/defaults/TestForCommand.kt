package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import java.util.stream.Collectors

class TestForCommand(name: String) : VanillaCommand(name, "commands.testfor.description") {
    init {
        this.permission = "chorus.command.testfor"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("victim", false, CommandParamType.TARGET)
            )
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val targets = result.value.getResult<List<Entity>>(0)!!
        if (targets.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        } else {
            log.addSuccess("commands.testfor.success", targets.stream().map<String> { entity: Entity ->
                var name = entity.getEntityName()
                if (name.isBlank()) name = entity.getOriginalName()
                name
            }.collect(Collectors.joining(","))).output()
            return targets.size
        }
    }
}
