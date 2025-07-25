package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.tree.node.XpLevelNode
import org.chorus_oss.chorus.command.utils.CommandLogger

class XpCommand(name: String) : Command(name, "commands.xp.description") {
    init {
        this.permission = "chorus.command.xp"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("amount", CommandParamType.INT),
            CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode())
        )
        commandParameters["level"] = arrayOf(
            CommandParameter.Companion.newType("level", CommandParamType.STRING, XpLevelNode()),
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
        //  "/xp <amount> [player]"  for adding exp
        //  "/xp <amount>L [player]" for adding exp level
        val list = result.value
        var players: List<Player>? = if (sender.isPlayer) listOf(sender.asPlayer()!!) else null
        when (result.key) {
            "default" -> {
                val amount = list.getResult<Int>(0)!!
                if (amount < 0) {
                    log.addError("commands.xp.failure.widthdrawXp").output()
                    return 0
                }
                if (list.hasResult(1)) {
                    players = list.getResult(1)
                }
                if (players == null || players.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                for (player in players) {
                    player.addExperience(amount)
                    log.addSuccess("commands.xp.success", amount.toString(), player.getEntityName())
                }
                log.successCount(players.size).output()
                return players.size
            }

            "level" -> {
                val level = list.getResult<Int>(0)!!
                if (list.hasResult(1)) {
                    players = list.getResult(1)
                }
                if (players == null || players.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                for (player in players) {
                    var newLevel = player.experienceLevel
                    newLevel += level
                    if (newLevel > 24791) newLevel = 24791
                    if (newLevel < 0) {
                        player.setExperience(0, 0)
                    } else {
                        player.setExperience(player.experience, newLevel, true)
                    }
                    log.addSuccess("commands.xp.success.levels", level.toString(), player.getEntityName())
                }
                log.successCount(players.size).output()
                return players.size
            }

            else -> {
                return 0
            }
        }
    }
}
