package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.event.player.PlayerKickEvent

class KickCommand(name: String) : VanillaCommand(name, "commands.kick.description") {
    init {
        this.permission = "chorus.command.kick"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newType("reason", true, CommandParamType.MESSAGE)
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
        val players = list.getResult<List<Player>>(0)!!
        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        var reason = ""
        if (list.hasResult(1)) {
            reason = list.getResult(1)!!
        }

        for (player in players) {
            player.kick(PlayerKickEvent.Reason.KICKED_BY_ADMIN, reason)
            if (reason.isNotEmpty()) {
                log.addSuccess("commands.kick.success.reason", player.getEntityName(), reason.toString())
            } else {
                log.addSuccess("commands.kick.success", player.getEntityName())
            }
        }
        log.successCount(players.size).output(true)
        return players.size
    }
}
