package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.IPStringNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.event.player.PlayerKickEvent
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern

class BanIpCommand(name: String) : VanillaCommand(name, "commands.banip.description", "%commands.banip.usage") {
    init {
        this.permission = "chorus.command.ban.ip"
        this.aliases = arrayOf("banip")
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("player", CommandParamType.STRING),
            CommandParameter.newType("reason", true, CommandParamType.STRING)
        )
        commandParameters["byIp"] = arrayOf(
            CommandParameter.newType("ip", CommandParamType.STRING, IPStringNode()),
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
        var reason = ""
        val list = result.value
        when (result.key) {
            "default" -> {
                var value = list.getResult<String>(0)!!
                if (list.hasResult(1)) reason = list.getResult(1)!!
                val player = Server.instance.getPlayer(value)
                if (player != null) {
                    this.processIPBan(player.address, sender, reason)
                    log.addSuccess("commands.banip.success.players", player.address, player.getEntityName())
                        .output(true)
                    return 1
                } else {
                    val name = value.lowercase()
                    val path = Server.instance.dataPath + "players/"
                    val file = File("$path$name.dat")
                    var nbt: CompoundTag? = null
                    if (file.exists()) {
                        try {
                            FileInputStream(file).use { inputStream ->
                                nbt = NBTIO.readCompressed(inputStream)
                            }
                        } catch (e: IOException) {
                            throw UncheckedIOException(e)
                        }
                    }

                    if (nbt != null && nbt!!.contains("lastIP") && Pattern.matches(
                            "^(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])$",
                            (nbt!!.getString("lastIP").also { value = it })
                        )
                    ) {
                        this.processIPBan(value, sender, reason)
                        log.addSuccess("commands.banip.success", value).output(true)
                        return 1
                    } else {
                        log.addError("commands.banip.invalid").output()
                        return 0
                    }
                }
            }

            "byIp" -> {
                val ip = list.getResult<String>(0)!!
                if (list.hasResult(1)) reason = list.getResult(1)!!
                this.processIPBan(ip, sender, reason)
                log.addSuccess("commands.banip.success", ip).output(true)
            }

            else -> {
                return 0
            }
        }
        return 0
    }

    private fun processIPBan(ip: String, sender: CommandSender, reason: String) {
        Server.instance.bannedIPs.addBan(ip, reason, null, sender.senderName)

        for (player in ArrayList(Server.instance.onlinePlayers.values)) {
            if (player.address == ip) {
                player.kick(PlayerKickEvent.Reason.IP_BANNED, reason.ifEmpty { "IP banned" })
            }
        }

        try {
            Server.instance.network.blockAddress(InetAddress.getByName(ip), -1)
        } catch (e: UnknownHostException) {
            // ignore
        }
    }
}
