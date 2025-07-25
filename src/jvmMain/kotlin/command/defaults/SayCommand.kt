package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer

class SayCommand(name: String) : VanillaCommand(name, "commands.say.description") {
    init {
        this.permission = "chorus.command.say"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("message", CommandParamType.MESSAGE)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val senderString = sender.senderName
        val message = result.value.getResult<String>(0)!!
        Server.instance.broadcastMessage(TranslationContainer("%chat.type.announcement", senderString, message))
        return 1
    }
}
