package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.IPlayer
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.IPlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class OpCommand(name: String) : VanillaCommand(name, "commands.op.description") {
    init {
        this.permission = "chorus.command.op.give"
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
            if (player.isOp) {
                log.addError("commands.op.failed", player.getEntityName()!!).output()
            } else {
                player.isOp = true
                if (player.isOnline) {
                    log.outputObjectWhisper(player.player!!, TextFormat.GRAY.toString() + "%commands.op.message")
                }
                log.addSuccess("commands.op.success", player.getEntityName()!!).output(true)
            }
        }
        return players.size
    }
}
