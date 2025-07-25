package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer

class ListCommand(name: String) : VanillaCommand(name, "commands.list.description") {
    init {
        this.permission = "chorus.command.list"
        commandParameters.clear()
        commandParameters["default"] = CommandParameter.Companion.EMPTY_ARRAY
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        var online = StringBuilder()
        var onlineCount = 0
        for (player in Server.instance.onlinePlayers.values) {
            if (player.isOnline && (sender !is Player || sender.canSee(player))) {
                online.append(player.displayName).append(", ")
                ++onlineCount
            }
        }
        if (online.isNotEmpty()) {
            online = StringBuilder(online.substring(0, online.length - 2))
        }
        sender.sendMessage(
            TranslationContainer(
                "commands.players.list",
                onlineCount.toString(), Server.instance.maxPlayers.toString()
            )
        )
        sender.sendMessage(online.toString())
        return 1
    }
}
