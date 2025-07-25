package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.GameRule

class DayLockCommand(name: String) :
    VanillaCommand(name, "commands.daylock.description", "", arrayOf<String>("alwaysday")) {
    init {
        this.permission = "chorus.command.daylock"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newEnum("lock", true, CommandEnum.Companion.ENUM_BOOLEAN)
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
        var lock = true

        if (list.hasResult(0)) lock = list.getResult(0)!!

        val level = sender.locator.level
        if (lock) {
            level.gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            level.stopTime()
            level.setTime(5000)
            log.addSuccess("commands.always.day.locked").output()
        } else {
            level.gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            level.startTime()
            log.addSuccess("commands.always.day.unlocked").output()
        }
        return 1
    }
}
