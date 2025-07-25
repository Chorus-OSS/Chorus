package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.IPlayer
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.IPlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger

class PardonCommand(name: String) : VanillaCommand(name, "unban a player") {
    init {
        this.permission = "chorus.command.unban.player"
        this.aliases = arrayOf("unban")
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("player", CommandParamType.TARGET, IPlayersNode())
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val players = result.value.getResult<List<IPlayer>>(0)!!
        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        for (player in players) {
            Server.instance.bannedPlayers.remove(player.getEntityName()!!)
            log.addSuccess("commands.unban.success", player.getEntityName()!!)
        }
        log.successCount(players.size).output(true)
        return players.size
    }
}
