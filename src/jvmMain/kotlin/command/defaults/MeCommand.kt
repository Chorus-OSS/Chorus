package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.utils.TextFormat

class MeCommand(name: String) : VanillaCommand(name, "commands.me.description", "chorus.command.me.usage") {
    init {
        this.permission = "chorus.command.me"
        commandParameters.clear()
        commandParameters["message"] = arrayOf(
            CommandParameter.Companion.newType("message", CommandParamType.MESSAGE)
        )
        commandParameters["default"] = CommandParameter.Companion.EMPTY_ARRAY
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val name = sender.senderName

        val message = if (result.key == "message") {
            result.value.getResult(0) ?: ""
        } else ""

        broadcastCommandMessage(
            sender,
            TranslationContainer("chat.type.emote", name, TextFormat.WHITE.toString() + message),
            true
        )
        return 1
    }
}
