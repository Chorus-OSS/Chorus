package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer

class WeatherCommand(name: String) : VanillaCommand(name, "commands.weather.description", "commands.weather.usage") {
    init {
        this.permission = "chorus.command.weather"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newEnum("type", CommandEnum("WeatherType", "clear", "rain", "thunder")),
            CommandParameter.Companion.newType("duration", true, CommandParamType.INT)
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
        val weather = list.getResult<String>(0)
        val level = sender.locator.level
        val seconds = if (list.hasResult(1)) {
            list.getResult(1)!!
        } else {
            600 * 20
        }
        when (weather) {
            "clear" -> {
                level.setRaining(false)
                level.setThundering(false)
                level.rainTime = seconds * 20
                level.thunderTime = seconds * 20
                log.addSuccess("commands.weather.clear").output(true)
                return 1
            }

            "rain" -> {
                level.setRaining(true)
                level.rainTime = seconds * 20
                log.addSuccess("commands.weather.rain").output(true)
                return 1
            }

            "thunder" -> {
                level.setThundering(true)
                level.rainTime = seconds * 20
                level.thunderTime = seconds * 20
                log.addSuccess("commands.weather.thunder").output(true)
                return 1
            }

            else -> {
                sender.sendMessage(
                    TranslationContainer(
                        "commands.generic.usage", """
     
     ${this.commandFormatTips}
     """.trimIndent()
                    )
                )
                return 0
            }
        }
    }
}
