package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger

class SaveCommand(name: String) : VanillaCommand(name, "Save the server (levels and players)") {
    init {
        this.permission = "chorus.command.save.perform"
        commandParameters.clear()
        commandParameters["default"] = CommandParameter.Companion.EMPTY_ARRAY
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        log.addSuccess("commands.save.start").output(true)
        for (player in Server.instance.onlinePlayers.values) {
            player.save()
        }
        for (level in Server.instance.levels.values) {
            level.save(true)
        }
        log.addSuccess("commands.save.success").output(true)
        return 1
    }
}
