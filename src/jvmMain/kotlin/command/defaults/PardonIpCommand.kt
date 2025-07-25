package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern


class PardonIpCommand(name: String) : VanillaCommand(name, "unban an IP") {
    init {
        this.permission = "chorus.command.unban.ip"
        this.aliases = arrayOf("unbanip", "unban-ip", "pardonip")
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("ip", CommandParamType.STRING)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val value = result.value.getResult<String>(0)!!
        if (Pattern.matches(
                "^(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])$",
                value
            )
        ) {
            Server.instance.bannedIPs.remove(value)
            try {
                Server.instance.network.unblockAddress(InetAddress.getByName(value))
            } catch (e: UnknownHostException) {
                log.addError("commands.unbanip.invalid").output()
                return 0
            }
            log.addSuccess("commands.unbanip.success", value).output(true)
            return 1
        } else {
            log.addError("commands.unbanip.invalid").output()
        }
        return 0
    }
}
