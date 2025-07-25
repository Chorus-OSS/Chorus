package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.permission.BanEntry
import org.chorus_oss.chorus.permission.BanList
import org.chorus_oss.chorus.utils.TextFormat

class BanListCommand(name: String) : VanillaCommand(name, "list all the banned players or IPs") {
    init {
        this.permission = "chorus.command.ban.list"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newEnum("type", true, CommandEnum("BanListType", "ips", "players"))
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val paramList = result.value
        val list: BanList
        var ips = false

        if (paramList.hasResult(0)) {
            val type = paramList.getResult<String>(0)
            when (type!!.lowercase()) {
                "ips" -> {
                    list = Server.instance.bannedIPs
                    ips = true
                }

                "players" -> list = Server.instance.bannedPlayers
                else -> {
                    log.addSyntaxErrors(0).output()
                    return 0
                }
            }
        } else {
            list = Server.instance.bannedPlayers
        }

        val builder = StringBuilder()
        val itr: Iterator<BanEntry> = list.entries.values.iterator()
        while (itr.hasNext()) {
            builder.append(itr.next().name)
            if (itr.hasNext()) {
                builder.append(", ")
            }
        }
        val size: Int = list.entries.size
        if (ips) {
            log.addSuccess("commands.banlist.ips", size.toString())
        } else {
            log.addSuccess("commands.banlist.players", size.toString())
        }
        log.addSuccess(TextFormat.GREEN.toString() + builder.toString()).successCount(size).output()
        return size
    }
}
