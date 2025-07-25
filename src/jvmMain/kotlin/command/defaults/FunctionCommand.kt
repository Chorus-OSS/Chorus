package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger

class FunctionCommand(name: String) : VanillaCommand(name, "commands.function.description") {
    init {
        this.permission = "chorus.command.function"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newEnum(
                "name",
                false,
                CommandEnum.Companion.FUNCTION_FILE
            ) //todo 找到CommandParamType.FILE_PATH自动补全的工作原理
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
        if (result.key == "default") {
            val file = list.getResult<String>(0)!!
            val function = Server.instance.functionManager.getFunction(file)
            if (function == null) {
                log.addError("commands.function.functionNameNotFound", file).output()
                return 0
            }
            function.dispatch(sender)
            log.addSuccess("commands.function.success", function.commands.size.toString()).output()
            return 1
        }
        return 0
    }
}
