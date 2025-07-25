package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType
import org.chorus_oss.chorus.utils.TextFormat
import java.text.DecimalFormat
import java.util.stream.Collectors

class SpawnpointCommand(name: String) : VanillaCommand(name, "commands.spawnpoint.description") {
    init {
        this.permission = "chorus.command.spawnpoint"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newType("spawnPos", true, CommandParamType.POSITION),
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
        var players = if (sender.isPlayer) listOf(sender.asPlayer()!!) else listOf()
        val round2 = DecimalFormat("##0.00")
        if (list.hasResult(0)) {
            players = list.getResult(0)!!
            if (players.isEmpty()) {
                log.addNoTargetMatch().output()
                return 0
            }
            val level = sender.locator.level
            if (list.hasResult(1)) {
                if (level != null) {
                    val locator = list.getResult<Locator>(1)
                    if (level.isOverWorld) {
                        if (locator!!.position.y < -64) locator.position.y = -64.0
                        if (locator.position.y > 320) locator.position.y = 320.0
                    } else {
                        if (locator!!.position.y < 0) locator.position.y = 0.0
                        if (locator.position.y > 255) locator.position.y = 255.0
                    }
                    for (player in players) {
                        player.setSpawn(locator, SpawnPointType.PLAYER)
                    }
                    log.addSuccess(
                        "commands.spawnpoint.success.multiple.specific",
                        players.stream().map { obj: Player -> obj.getEntityName() }.collect(
                            Collectors.joining(" ")
                        ),
                        round2.format(locator.position.x),
                        round2.format(locator.position.y),
                        round2.format(locator.position.z)
                    ).successCount(players.size).output(true)
                    return players.size
                }
            }
            log.addSyntaxErrors(1).output()
            return 0
        }
        if (players.isNotEmpty()) {
            val pos = players[0].locator
            players[0].setSpawn(pos, SpawnPointType.PLAYER)
            log.addSuccess(
                "commands.spawnpoint.success.single", sender.senderName,
                round2.format(pos.position.x),
                round2.format(pos.position.y),
                round2.format(pos.position.z)
            ).output(true)
            return 1
        } else {
            sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.noTargetMatch"))
            return 0
        }
    }
}
