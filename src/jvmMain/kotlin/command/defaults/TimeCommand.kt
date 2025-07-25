package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Level

class TimeCommand(name: String) : VanillaCommand(name, "commands.time.description") {
    init {
        this.permission = "chorus.command.time.add;" +
                "chorus.command.time.set;" +
                "chorus.command.time.start;" +
                "chorus.command.time.stop"
        commandParameters.clear()
        commandParameters["1arg"] = arrayOf(
            CommandParameter.Companion.newEnum("mode", CommandEnum("TimeMode", "query", "start", "stop"))
        )
        commandParameters["add"] = arrayOf(
            CommandParameter.Companion.newEnum("mode", CommandEnum("TimeModeAdd", "add")),
            CommandParameter.Companion.newType("amount", CommandParamType.INT)
        )
        commandParameters["setAmount"] = arrayOf(
            CommandParameter.Companion.newEnum("mode", false, CommandEnum("TimeModeSet", "set")),
            CommandParameter.Companion.newType("amount", CommandParamType.INT)
        )
        commandParameters["setTime"] = arrayOf(
            CommandParameter.Companion.newEnum("mode", CommandEnum("TimeModeSet", "set")),
            CommandParameter.Companion.newEnum(
                "time",
                CommandEnum("TimeSpec", "day", "night", "midnight", "noon", "sunrise", "sunset")
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
        when (result.key) {
            "1arg" -> {
                val mode = list.getResult<String>(0)
                if ("start" == mode) {
                    if (!sender.hasPermission("chorus.command.time.start")) {
                        log.addMessage("chorus.command.generic.permission").output()
                        return 0
                    }
                    for (level in Server.instance.levels.values) {
                        level.checkTime()
                        level.startTime()
                        level.checkTime()
                    }
                    log.addSuccess("Restarted the time").output(true)
                } else if ("stop" == mode) {
                    if (!sender.hasPermission("chorus.command.time.stop")) {
                        log.addMessage("chorus.command.generic.permission").output()
                        return 0
                    }
                    for (level in Server.instance.levels.values) {
                        level.checkTime()
                        level.stopTime()
                        level.checkTime()
                    }
                    log.addSuccess("Stopped the time").output(true)
                } else if ("query" == mode) {
                    if (!sender.hasPermission("chorus.command.time.query")) {
                        log.addMessage("chorus.command.generic.permission").output()
                        return 0
                    }
                    val level = if (sender is Player) {
                        sender.level!!
                    } else {
                        Server.instance.defaultLevel!!
                    }
                    log.addSuccess("commands.time.query.gametime", level.getTime().toString()).output(true)
                }
                return 1
            }

            "add" -> {
                if (!sender.hasPermission("chorus.command.time.add")) {
                    log.addMessage("chorus.command.generic.permission").output()
                    return 0
                }
                val value = list.getResult<Int>(1)!!
                if (value < 0) {
                    log.addNumTooSmall(1, 0).output()
                    return 0
                }
                for (level in Server.instance.levels.values) {
                    level.checkTime()
                    level.setTime(level.getTime() + value)
                    level.checkTime()
                }
                log.addSuccess("commands.time.added", value.toString()).output(true)
                return 1
            }

            "setAmount" -> {
                if (!sender.hasPermission("chorus.command.time.set")) {
                    log.addMessage("chorus.command.generic.permission").output()
                    return 0
                }
                val value = list.getResult<Int>(1)!!
                if (value < 0) {
                    log.addNumTooSmall(1, 0).output()
                    return 0
                }
                for (level in Server.instance.levels.values) {
                    level.checkTime()
                    level.setTime(value)
                    level.checkTime()
                }
                log.addSuccess("commands.time.set", value.toString()).output(true)
                return 1
            }

            "setTime" -> {
                if (!sender.hasPermission("chorus.command.time.set")) {
                    log.addMessage("chorus.command.generic.permission").output()
                    return 0
                }
                var value = 0
                val str = list.getResult<String>(1)
                when (str) {
                    "day" -> value = Level.TIME_DAY
                    "night" -> value = Level.TIME_NIGHT
                    "midnight" -> value = Level.TIME_MIDNIGHT
                    "noon" -> value = Level.TIME_NOON
                    "sunrise" -> value = Level.TIME_SUNRISE
                    "sunset" -> value = Level.TIME_SUNSET
                }
                for (level in Server.instance.levels.values) {
                    level.checkTime()
                    level.setTime(value)
                    level.checkTime()
                }
                log.addSuccess("commands.time.set", value.toString()).output(true)
                return 1
            }

            else -> {
                return 0
            }
        }
    }
}
