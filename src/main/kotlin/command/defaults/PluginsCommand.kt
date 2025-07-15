package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class PluginsCommand(name: String) : Command(
    name,
    "%chorus.command.plugins.description",
    "%chorus.command.plugins.usage",
    arrayOf<String>("pl")
), CoreCommand {
    init {
        this.permission = "chorus.command.plugins"
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
        this.sendPluginList(sender, log)
        return 1
    }

    private fun sendPluginList(sender: CommandSender, log: CommandLogger) {
        val list = StringBuilder()
        val plugins = Server.instance.pluginManager.plugins
        for (plugin in plugins.values) {
            if (list.isNotEmpty()) {
                list.append(TextFormat.WHITE.toString() + ", ")
            }
            list.append(if (plugin.isEnabled) TextFormat.GREEN else TextFormat.RED)
            list.append(plugin.description.versionedName)
        }
        log.addMessage("chorus.command.plugins.success", plugins.size.toString(), list.toString()).output()
    }
}
