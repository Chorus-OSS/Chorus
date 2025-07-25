package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer

class TellCommand(name: String) :
    VanillaCommand(name, "commands.tell.description", "", arrayOf<String>("w", "msg")) {
    init {
        this.permission = "chorus.command.tell"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newType("message", CommandParamType.MESSAGE)
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
        val msg = list.getResult<String>(1)!!
        for (player in players) {
            if (player === sender) {
                log.addError("commands.message.sameTarget").output()
                continue
            }
            log.addSuccess("commands.message.display.outgoing", player.getEntityName(), msg)
            player.sendMessage(TranslationContainer("commands.message.display.incoming", sender.senderName, msg))
        }
        log.output()
        return 1
    }
}
