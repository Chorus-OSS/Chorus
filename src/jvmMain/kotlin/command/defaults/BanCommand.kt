package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.event.player.PlayerKickEvent

class BanCommand(name: String) : VanillaCommand(name, "commands.ban.description", "%commands.ban.usage") {
    init {
        this.permission = "chorus.command.ban.player"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("player", CommandParamType.STRING),
            CommandParameter.newType("reason", true, CommandParamType.STRING)
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
        val name = list.getResult<String>(0)!!
        val reason = list.getResult<String>(1)
        Server.instance.bannedPlayers.addBan(name, reason, null, sender.senderName)

        val player = Server.instance.getPlayerExact(name)
        player?.kick(
            PlayerKickEvent.Reason.NAME_BANNED,
            if (reason!!.isNotEmpty()) "Banned by admin. Reason: $reason" else "Banned by admin"
        )
        log.addSuccess("commands.ban.success", player?.getEntityName() ?: name).output(true)
        return 1
    }
}
