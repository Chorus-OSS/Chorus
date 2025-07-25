package org.chorus_oss.chorus.command.defaults

import com.google.gson.JsonSyntaxException
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.utils.TextFormat

class TellrawCommand(name: String) : VanillaCommand(name, "commands.tellraw.description") {
    init {
        this.permission = "chorus.command.tellraw"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newType("rawtext", CommandParamType.RAWTEXT)
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
        try {
            val players = list.getResult<List<Player>>(0)!!
            if (players.isEmpty()) {
                log.addNoTargetMatch().output()
                return 0
            }
            val rawTextObject = list.getResult<org.chorus_oss.chorus.command.utils.RawText>(1)
            rawTextObject!!.preParse(sender)
            for (player in players) {
                player.sendRawTextMessage(rawTextObject)
            }
            return 1
        } catch (e: JsonSyntaxException) {
            sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.tellraw.jsonStringException"))
            return 0
        }
    }
}
