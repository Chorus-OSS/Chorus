package org.chorus_oss.chorus.command.simple

import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.ConsoleCommandSender
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.utils.Loggable
import java.lang.reflect.Method

class SimpleCommand(
    private val `object`: Any,
    private val method: Method,
    name: String,
    description: String,
    usageMessage: String?,
    aliases: Array<String>
) :
    Command(name, description, usageMessage, aliases) {
    private var forbidConsole = false
    private var maxArgs = 0
    private var minArgs = 0

    fun setForbidConsole(forbidConsole: Boolean) {
        this.forbidConsole = forbidConsole
    }

    fun setMaxArgs(maxArgs: Int) {
        this.maxArgs = maxArgs
    }

    fun setMinArgs(minArgs: Int) {
        this.minArgs = minArgs
    }

    fun sendUsageMessage(sender: CommandSender) {
        if (this.usage != "") {
            sender.sendMessage(TranslationContainer("commands.generic.usage", this.usage))
        }
    }

    fun sendInGameMessage(sender: CommandSender) {
        sender.sendMessage(TranslationContainer("chorus.command.generic.ingame"))
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        if (this.forbidConsole && sender is ConsoleCommandSender) {
            this.sendInGameMessage(sender)
            return false
        } else if (!this.testPermission(sender)) {
            return false
        } else if (this.maxArgs != 0 && args.size > this.maxArgs) {
            this.sendUsageMessage(sender)
            return false
        } else if (this.minArgs != 0 && args.size < this.minArgs) {
            this.sendUsageMessage(sender)
            return false
        }

        var success = false

        try {
            success = method.invoke(this.`object`, sender, commandLabel, args) as Boolean
        } catch (exception: Exception) {
            log.error("Failed to execute {} by {}", commandLabel, sender.senderName, exception)
        }

        if (!success) {
            this.sendUsageMessage(sender)
        }

        return success
    }

    companion object : Loggable
}
