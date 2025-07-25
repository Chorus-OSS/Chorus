package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.ConsoleCommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.utils.TextFormat
import kotlin.math.min


class HelpCommand(name: String) :
    VanillaCommand(name, "commands.help.description", "%commands.help.usage", arrayOf<String>("?")) {
    init {
        this.permission = "chorus.command.help"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("page", true, CommandParamType.INT)
        )
        this.isServerSideOnly = true
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        var args = args
        if (!this.testPermission(sender)) {
            return false
        }
        val command = StringBuilder()
        var pageNumber = 1
        var pageHeight = 5
        if (args.size != 0) {
            try {
                pageNumber = args[args.size - 1]!!.toInt()
                if (pageNumber <= 0) {
                    pageNumber = 1
                }

                val newargs = arrayOfNulls<String>(args.size - 1)
                System.arraycopy(args, 0, newargs, 0, newargs.size)
                args = newargs
                /*if (args.length > 1) {
                    args = Arrays.copyOfRange(args, 0, args.length - 2);
                } else {
                    args = new String[0];
                }*/
                for (arg in args) {
                    if (command.toString() != "") {
                        command.append(" ")
                    }
                    command.append(arg)
                }
            } catch (e: NumberFormatException) {
                pageNumber = 1
                for (arg in args) {
                    if (command.toString() != "") {
                        command.append(" ")
                    }
                    command.append(arg)
                }
            }
        }

        if (sender is ConsoleCommandSender) {
            pageHeight = Int.MAX_VALUE
        }

        if (command.toString() == "") {
            val commands: MutableMap<String?, Command> = mutableMapOf()
            for (cmd in Server.instance.commandMap.commands.values) {
                if (cmd.testPermissionSilent(sender)) {
                    commands[cmd.name] = cmd
                }
            }
            val totalPage =
                if (commands.size % pageHeight == 0) commands.size / pageHeight else commands.size / pageHeight + 1
            pageNumber = min(pageNumber.toDouble(), totalPage.toDouble()).toInt()
            if (pageNumber < 1) {
                pageNumber = 1
            }

            sender.sendMessage(
                TranslationContainer(
                    TextFormat.DARK_GREEN.toString() + "%commands.help.header",
                    pageNumber.toString(),
                    totalPage.toString()
                )
            )
            var i = 1
            for (command1 in commands.values) {
                if (i >= (pageNumber - 1) * pageHeight + 1 && i <= min(
                        commands.size.toDouble(),
                        (pageNumber * pageHeight).toDouble()
                    )
                ) {
                    sender.sendMessage(command1.commandFormatTips)
                }
                i++
            }
            sender.sendMessage(TranslationContainer(TextFormat.DARK_GREEN.toString() + "%commands.help.footer"))

            return true
        } else {
            val cmd = Server.instance.commandMap.getCommand(command.toString().lowercase())
            if (cmd != null) {
                if (cmd.testPermissionSilent(sender)) {
                    sender.sendMessage(TextFormat.YELLOW.toString() + cmd.name)
                    sender.sendMessage(TranslationContainer(TextFormat.YELLOW.toString() + "%" + cmd.description))
                    sender.sendMessage(TranslationContainer("commands.generic.usage.noparam"))
                    sender.sendMessage(cmd.commandFormatTips)
                    return true
                }
            }

            sender.sendMessage(TextFormat.RED.toString() + "No help for " + command.toString().lowercase())
            return false
        }
    }
}
