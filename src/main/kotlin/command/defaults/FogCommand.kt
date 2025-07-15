package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.utils.Identifier
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class FogCommand(name: String) : VanillaCommand(name, "commands.fog.description", "commands.fog.usage") {
    init {
        this.permission = "chorus.command.fog"
        commandParameters.clear()
        commandParameters["push"] = arrayOf(
            CommandParameter.Companion.newType("victim", CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("push", arrayOf("push")),
            CommandParameter.Companion.newType("fogId", CommandParamType.STRING),
            CommandParameter.Companion.newType("userProvidedId", CommandParamType.STRING)
        )
        commandParameters["delete"] = arrayOf(
            CommandParameter.Companion.newType("victim", CommandParamType.TARGET, PlayersNode()),
            CommandParameter.Companion.newEnum("mode", CommandEnum("delete", "pop", "remove")),
            CommandParameter.Companion.newType("userProvidedId", CommandParamType.STRING)
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
        val targets = list.getResult<List<Player>>(0)!!
        if (targets.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        when (result.key) {
            "push" -> {
                val fogIdStr = list.getResult<String>(2)!!
                if (Identifier.tryParse(fogIdStr) == null) {
                    log.addError("commands.fog.invalidFogId", fogIdStr).output()
                    return 0
                }
                val userProvidedId = list.getResult<String>(3)!!
                targets.forEach(Consumer<Player> { player: Player ->
                    player.fogStack.add(Pair(userProvidedId, fogIdStr))
                    player.sendFogStack() //刷新到客户端
                })
                log.addSuccess("commands.fog.success.push", userProvidedId, fogIdStr).output()
                return 1
            }

            "delete" -> {
                val mode = list.getResult<String>(1)
                val userProvidedId = list.getResult<String>(2)!!
                val success = AtomicInteger(1)
                when (mode) {
                    "pop" -> {
                        targets.forEach(Consumer forEach@{ player: Player ->
                            val fogStack = player.fogStack
                            for (i in fogStack.indices.reversed()) {
                                val fog = fogStack[i]
                                if (fog.first == userProvidedId) {
                                    fogStack.removeAt(i)
                                    player.sendFogStack() //刷新到客户端
                                    log.addSuccess(
                                        "commands.fog.success.pop",
                                        userProvidedId,
                                        fog.second
                                    ).output()
                                    return@forEach
                                }
                            }
                            log.addError("commands.fog.invalidUserId", userProvidedId).output()
                            success.set(0)
                        })
                        return success.get()
                    }

                    "remove" -> {
                        targets.forEach { player: Player ->
                            val fogStack = player.fogStack
                            val shouldRemoved: MutableList<Pair<String, String>> = mutableListOf()
                            for (fog in fogStack) {
                                if (fog.first == userProvidedId) {
                                    shouldRemoved.add(fog)
                                    log.addSuccess(
                                        "commands.fog.success.remove",
                                        userProvidedId,
                                        fog.second
                                    ).output()
                                }
                            }
                            fogStack.removeAll(shouldRemoved)
                            player.sendFogStack() //刷新到客户端
                            if (shouldRemoved.isEmpty()) {
                                log.addError("commands.fog.invalidUserId", userProvidedId).output()
                                success.set(0)
                            }
                        }
                        return success.get()
                    }

                    else -> {
                        return 0
                    }
                }
            }

            else -> {
                return 0
            }
        }
    }
}
