package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.TextFormat

class TitlerawCommand(name: String) : VanillaCommand(name, "commands.titleraw.description") {
    init {
        this.permission = "chorus.command.titleraw"
        commandParameters.clear()
        commandParameters["clear"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("clear", CommandEnum("TitleClear", "clear"))
        )
        commandParameters["reset"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("reset", CommandEnum("TitleReset", "reset"))
        )
        commandParameters["set"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum(
                "titleLocation",
                CommandEnum("TitleSet", "title", "subtitle", "actionbar")
            ),
            CommandParameter.Companion.newType("titleJson", CommandParamType.RAWTEXT)
        )
        commandParameters["times"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("times", CommandEnum("TitleTimes", "times")),
            CommandParameter.Companion.newType("fadeIn", CommandParamType.INT),
            CommandParameter.Companion.newType("stay", CommandParamType.INT),
            CommandParameter.Companion.newType("fadeOut", CommandParamType.INT)
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
        val players = list.getResult<List<Player>>(0)!!
        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }

        when (result.key) {
            "clear" -> {
                for (player in players) {
                    player.clearTitle()
                    log.addMessage(TextFormat.WHITE.toString() + "%chorus.command.title.clear", player.getEntityName())
                }
                log.output()
                return 1
            }

            "reset" -> {
                for (player in players) {
                    player.resetTitleSettings()
                    log.addMessage(TextFormat.WHITE.toString() + "%chorus.command.title.reset", player.getEntityName())
                }
                log.output()
                return 1
            }

            "set" -> {
                val titleLocation = list.getResult<String>(1)
                val rawText = list.getResult<org.chorus_oss.chorus.command.utils.RawText>(2)!!
                when (titleLocation) {
                    "title" -> {
                        for (player in players) {
                            player.setRawTextTitle(rawText)
                            log.addSuccess("commands.titleraw.success")
                        }
                        log.output()
                    }

                    "subtitle" -> {
                        for (player in players) {
                            player.setRawTextSubTitle(rawText)
                            log.addSuccess("commands.titleraw.success")
                        }
                        log.output()
                    }

                    "actionbar" -> {
                        for (player in players) {
                            player.setRawTextActionBar(rawText)
                            log.addSuccess("commands.titleraw.success")
                        }
                        log.output()
                    }

                    else -> {
                        log.addMessage(
                            "commands.generic.usage", """
     
     ${this.commandFormatTips}
     """.trimIndent()
                        )
                        return 0
                    }
                }
                return 1
            }

            "times" -> {
                val fadeIn = list.getResult<Int>(2)!!
                val stay = list.getResult<Int>(3)!!
                val fadeOut = list.getResult<Int>(4)!!
                for (player in players) {
                    log.addMessage(
                        TextFormat.WHITE.toString() + "%chorus.command.title.times.success",
                        fadeIn.toString(),
                        stay.toString(),
                        fadeOut.toString(),
                        player.getEntityName()
                    )
                }
                log.output()
                return 1
            }

            else -> {
                return 0
            }
        }
    }
}
