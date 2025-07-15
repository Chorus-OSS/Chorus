package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class GamemodeCommand(name: String) : VanillaCommand(
    name, "commands.gamemode.description", "",
    arrayOf("gm")
) {
    init {
        this.permission = "chorus.command.gamemode.survival;" +
                "chorus.command.gamemode.creative;" +
                "chorus.command.gamemode.adventure;" +
                "chorus.command.gamemode.spectator;" +
                "chorus.command.gamemode.other"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("gameMode", CommandParamType.INT),
            CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode())
        )
        commandParameters["byString"] = arrayOf(
            CommandParameter.Companion.newEnum("gameMode", CommandEnum.Companion.ENUM_GAMEMODE),
            CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode())
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
        var gameMode = -1
        when (result.key) {
            "default" -> gameMode = list.getResult(0)!!
            "byString" -> {
                val str = list.getResult<String>(0)!!
                gameMode = Server.getGamemodeFromString(str)
            }
        }
        if (gameMode < 0 || gameMode > 3) {
            log.addError("commands.gamemode.fail.invalid", gameMode.toString()).output()
            return 0
        }
        val players =
            if (list.hasResult(1)) {
                if (sender.hasPermission("chorus.command.gamemode.other")) {
                    list.getResult<List<Player>>(1)!!
                } else {
                    log.addMessage(TextFormat.RED.toString() + "%chorus.command.generic.permission").output()
                    return 0
                }
            } else {
                if (sender.isPlayer) listOf(sender.asPlayer()!!) else listOf()
            }

        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }

        if ((gameMode == 0 && !sender.hasPermission("chorus.command.gamemode.survival")) ||
            (gameMode == 1 && !sender.hasPermission("chorus.command.gamemode.creative")) ||
            (gameMode == 2 && !sender.hasPermission("chorus.command.gamemode.adventure")) ||
            (gameMode == 3 && !sender.hasPermission("chorus.command.gamemode.spectator"))
        ) {
            log.addMessage(TextFormat.RED.toString() + "%chorus.command.generic.permission").output()
            return 0
        }

        for (target in players) {
            target.setGamemode(gameMode)
            if (target == sender.asPlayer()) {
                log.addSuccess("commands.gamemode.success.self", Server.getGamemodeString(gameMode))
            } else {
                log.outputObjectWhisper(target, "gameMode.changed", Server.getGamemodeString(gameMode))
                log.addSuccess(
                    "commands.gamemode.success.other",
                    Server.getGamemodeString(gameMode),
                    target.getEntityName()
                )
            }
        }
        log.successCount(players.size).output(true)
        return players.size
    }
}
