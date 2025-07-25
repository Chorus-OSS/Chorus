package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.IPlayer
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.IPlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class DeOpCommand(name: String) : VanillaCommand(name, "commands.deop.description") {
    init {
        this.permission = "chorus.command.op.take"
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
            if (!player.isOp) {
                log.addError("Privileges cannot be revoked (revoked or with higher privileges)")
                    .output() //no translation in client
                return 0
            }
            player.isOp = false
            if (player.isOnline) {
                log.outputObjectWhisper(player.player!!, TextFormat.GRAY.toString() + "%commands.deop.message")
            }
            log.addSuccess("commands.deop.success", player.getEntityName()!!).output(true)
        }
        return players.size
    }
}
