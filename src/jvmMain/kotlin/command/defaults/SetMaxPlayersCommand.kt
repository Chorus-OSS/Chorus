package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger

class SetMaxPlayersCommand(name: String) : VanillaCommand(name, "commands.setmaxplayers.description") {
    init {
        this.permission = "chorus.command.setmaxplayers"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("maxPlayers", false, CommandParamType.INT)
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
        var maxPlayers = result.value.getResult<Int>(0)!!
        var lowerBound = false

        if (maxPlayers < Server.instance.onlinePlayers.size) {
            maxPlayers = Server.instance.onlinePlayers.size
            lowerBound = true
        }

        Server.instance.setMaxPlayers(maxPlayers)
        log.addSuccess("commands.setmaxplayers.success", maxPlayers.toString())
        if (lowerBound) {
            log.addSuccess("commands.setmaxplayers.success.lowerbound")
        }
        log.output()
        return 1
    }
}
