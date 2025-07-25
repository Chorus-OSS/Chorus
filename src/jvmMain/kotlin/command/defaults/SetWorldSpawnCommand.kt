package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import java.text.DecimalFormat

class SetWorldSpawnCommand(name: String) : VanillaCommand(name, "commands.setworldspawn.description") {
    init {
        this.permission = "chorus.command.setworldspawn"
        commandParameters.clear()
        commandParameters["default"] = CommandParameter.Companion.EMPTY_ARRAY
        commandParameters["spawnPoint"] = arrayOf(
            CommandParameter.Companion.newType("spawnPoint", true, CommandParamType.POSITION)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val level: Level
        val pos: Vector3
        if (!result.value.hasResult(0)) {
            level = sender.locator.level
            pos = sender.locator.position.round()
        } else {
            level = Server.instance.defaultLevel!!
            pos = result.value.getResult(0)!!
        }
        level.setSpawnLocation(pos)
        val round2 = DecimalFormat("##0.00")
        log.addSuccess(
            "commands.setworldspawn.success", round2.format(pos.x),
            round2.format(pos.y),
            round2.format(pos.z)
        ).output(true)
        return 1
    }
}
