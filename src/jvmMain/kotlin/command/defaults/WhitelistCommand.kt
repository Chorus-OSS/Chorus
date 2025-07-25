package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.StringNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class WhitelistCommand(name: String) :
    VanillaCommand(
        name,
        "chorus.command.whitelist.description",
        "chorus.command.allowlist.usage",
        arrayOf<String>("allowlist")
    ) {
    init {
        this.permission = "chorus.command.whitelist.reload;" +
                "chorus.command.whitelist.enable;" +
                "chorus.command.whitelist.disable;" +
                "chorus.command.whitelist.list;" +
                "chorus.command.whitelist.add;" +
                "chorus.command.whitelist.remove;" +  //v1.18.10+
                "chorus.command.allowlist.reload;" +
                "chorus.command.allowlist.enable;" +
                "chorus.command.allowlist.disable;" +
                "chorus.command.allowlist.list;" +
                "chorus.command.allowlist.add;" +
                "chorus.command.allowlist.remove"
        commandParameters.clear()
        commandParameters["1arg"] = arrayOf(
            CommandParameter.Companion.newEnum("action", CommandEnum("AllowlistAction", "on", "off", "list", "reload"))
        )
        commandParameters["2args"] = arrayOf(
            CommandParameter.Companion.newEnum("action", CommandEnum("AllowlistPlayerAction", "add", "remove")),
            CommandParameter.Companion.newType("player", CommandParamType.TARGET, StringNode())
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
        when (result.key) {
            "1arg" -> {
                val action = list.getResult<String>(0)
                if (this.badPerm(log, sender, action!!.lowercase())) {
                    return 0
                }
                when (action.lowercase()) {
                    "reload" -> {
                        Server.instance.reloadWhitelist()
                        log.addSuccess("commands.allowlist.reloaded").output(true)
                        return 1
                    }

                    "on" -> {
                        Server.instance.settings.serverSettings.whiteList = true
                        log.addSuccess("commands.allowlist.enabled").output(true)
                        return 1
                    }

                    "off" -> {
                        Server.instance.settings.serverSettings.whiteList = false
                        log.addSuccess("commands.allowlist.disabled").output(true)
                        return 1
                    }

                    "list" -> {
                        val re = StringBuilder()
                        var count = 0
                        for (player in Server.instance.whitelist.all.keys) {
                            re.append(player).append(", ")
                            ++count
                        }
                        log.addSuccess("commands.allowlist.list", count.toString(), count.toString())
                        log.addSuccess(if (re.length > 0) re.substring(0, re.length - 2) else "").output()
                        return 1
                    }
                }
            }

            "2args" -> {
                val action = list.getResult<String>(0)
                val name = list.getResult<String>(1)!!
                if (this.badPerm(log, sender, action!!.lowercase())) {
                    return 0
                }
                when (action.lowercase()) {
                    "add" -> {
                        Server.instance.getOfflinePlayer(name)?.isWhitelisted = true
                        log.addSuccess("commands.allowlist.add.success", name).output(true)
                        return 1
                    }

                    "remove" -> {
                        Server.instance.getOfflinePlayer(name)?.isWhitelisted = false
                        log.addSuccess("commands.allowlist.remove.success", name).output(true)
                        return 1
                    }
                }
            }

            else -> {
                return 0
            }
        }
        return 1
    }

    private fun badPerm(log: CommandLogger, sender: CommandSender, perm: String): Boolean {
        if (!sender.hasPermission("chorus.command.whitelist.$perm") && !sender.hasPermission("chorus.command.allowlist.$perm")) {
            log.addMessage(TextFormat.RED.toString() + "%chorus.command.generic.permission").output()
            return true
        }
        return false
    }
}
