package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.utils.TextFormat
import java.util.function.Consumer

class WorldCommand(name: String) : VanillaCommand(name, "chorus.command.world.description") {
    init {
        this.permission = "chorus.command.world"
        commandParameters.clear()
        commandParameters["tp"] = arrayOf(
            CommandParameter.Companion.newEnum("tp", arrayOf("tp")),
            CommandParameter.Companion.newEnum("world", false, WORLD_NAME_ENUM)
        )
        commandParameters["list"] = arrayOf(
            CommandParameter.Companion.newEnum("list", arrayOf("list"))
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        when (result.key) {
            "list" -> {
                val strBuilder = StringBuilder()
                Server.instance.levels.values.forEach(Consumer { level: Level ->
                    strBuilder.append(level.name)
                    strBuilder.append(", ")
                })
                log.addMessage(
                    TextFormat.WHITE.toString() + "%chorus.command.world.availableLevels",
                    strBuilder.toString()
                ).output()
                return 1
            }

            "tp" -> {
                val levelName = result.value.getResult<String>(1)!!
                var level = Server.instance.getLevelByName(levelName)
                if (level == null) {
                    if (Server.instance.loadLevel(levelName)) {
                        level = Server.instance.getLevelByName(levelName)
                    } else {
                        log.addMessage("chorus.command.world.levelNotFound", levelName).output()
                        return 0
                    }
                }
                sender.asEntity()!!.teleport(level!!.safeSpawn)
                log.addMessage(TextFormat.WHITE.toString() + "%chorus.command.world.successTp", levelName).output()
                return 1
            }

            else -> {
                return 0
            }
        }
    }

    companion object {
        @JvmField
        val WORLD_NAME_ENUM: CommandEnum = CommandEnum(
            "world"
        ) {
            Server.instance.levels.values.stream()
                .map { obj: Level -> obj.name }.toList()
        }
    }
}
