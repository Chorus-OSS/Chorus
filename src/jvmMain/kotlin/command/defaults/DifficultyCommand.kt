package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger

class DifficultyCommand(name: String) :
    VanillaCommand(name, "commands.difficulty.description", "%commands.difficulty.usage") {
    init {
        this.permission = "chorus.command.difficulty"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("difficulty", CommandParamType.INT)
        )
        commandParameters["byString"] = arrayOf(
            CommandParameter.Companion.newEnum(
                "difficulty",
                CommandEnum("Difficulty", "peaceful", "p", "easy", "e", "normal", "n", "hard", "h")
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
        var difficulty: Int
        when (result.key) {
            "default" -> {
                difficulty = list.getResult(0)!!
            }

            "byString" -> {
                val str = list.getResult<String>(0)!!
                difficulty = Server.getDifficultyFromString(str)
            }

            else -> {
                return 0
            }
        }
        if (Server.instance.isHardcore) {
            difficulty = 3
        }
        if (difficulty != -1) {
            Server.instance.setDifficulty(difficulty)
            val pk = org.chorus_oss.protocol.packets.SetDifficultyPacket(
                difficulty = Server.instance.getDifficulty().toUInt()
            )
            Server.broadcastPacket(Server.instance.onlinePlayers.values, pk)
            log.addSuccess("commands.difficulty.success", difficulty.toString()).output(true)
            return 1
        } else {
            log.addSyntaxErrors(0).output()
            return 0
        }
    }
}
