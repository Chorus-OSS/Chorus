package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.BooleanNode
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger

class AbilityCommand(name: String) : VanillaCommand(name, "commands.ability.description", "%commands.ability.usage") {
    init {
        this.permission = "chorus.command.ability"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("player", false, CommandParamType.TARGET, PlayersNode()),
            CommandParameter.newEnum(
                "ability",
                false,
                arrayOf("mayfly", "mute", "worldbuilder")
            ),
            CommandParameter.newEnum(
                "value",
                true,
                CommandEnum.ENUM_BOOLEAN,
                BooleanNode()
            )
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
        val ability_str: String
        val type: AdventureSettings.Type? = when (list.getResult<String>(1).also { ability_str = it!! }) {
            "mayfly" -> AdventureSettings.Type.ALLOW_FLIGHT
            "mute" -> AdventureSettings.Type.MUTED
            "worldbuilder" -> AdventureSettings.Type.WORLD_BUILDER
            else -> null
        }
        if (type == null) return 0

        if (list.hasResult(2)) {
            val value = list.getResult<Boolean>(2)!!
            for (player in players) {
                player.adventureSettings.set(type, value)
                player.adventureSettings.update()
                if (value) log.addSuccess("commands.ability.granted", ability_str)
                else log.addSuccess("commands.ability.revoked", ability_str)
            }
            log.addSuccess("commands.ability.success").successCount(1).output()
        } else {
            if (!sender.isPlayer) {
                return 0
            }
            val value: Boolean = sender.asPlayer()!!.adventureSettings.get(type)
            log.addSuccess("$ability_str = $value").output()
        }
        return 1
    }
}
