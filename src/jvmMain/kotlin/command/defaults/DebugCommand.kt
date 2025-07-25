package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.ai.EntityAI
import org.chorus_oss.chorus.item.ItemFilledMap
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.scheduler.AsyncTask

class DebugCommand(name: String) : TestCommand(name, "commands.debug.description"),
    CoreCommand {
    init {
        this.permission = "chorus.command.debug"
        commandParameters.clear()
        //生物AI debug模式开关
        commandParameters["entity"] =
            arrayOf(
                CommandParameter.Companion.newEnum("entity", arrayOf("entity")),
                CommandParameter.Companion.newEnum(
                    "option",
                    EntityAI.DebugOption.entries.map { option: EntityAI.DebugOption -> option.name.lowercase() }
                        .toTypedArray()
                ),
                CommandParameter.Companion.newEnum("value", false, CommandEnum.Companion.ENUM_BOOLEAN)
            )
        commandParameters["rendermap"] = arrayOf(
            CommandParameter.Companion.newEnum("rendermap", arrayOf("rendermap")),
            CommandParameter.Companion.newType("zoom", CommandParamType.INT)
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
            "entity" -> {
                val str = list.getResult<String>(1)
                val option: EntityAI.DebugOption = EntityAI.DebugOption.valueOf(str!!.uppercase())
                val value = list.getResult<Boolean>(2)!!
                EntityAI.setDebugOption(option, value)
                log.addSuccess(
                    "Entity AI framework " + option.name + " debug mode have been set to: " + EntityAI.checkDebugOption(
                        option
                    )
                ).output()
                return 1
            }

            "rendermap" -> {
                if (!sender.isPlayer) return 0
                val zoom = list.getResult<Int>(1)!!
                if (zoom < 1) {
                    log.addError("Zoom must bigger than one").output()
                    return 0
                }
                val player = sender.asPlayer()
                if (player!!.inventory.itemInHand is ItemFilledMap) {
                    val itemFilledMap = player.inventory.itemInHand as ItemFilledMap
                    player.level!!.scheduler.scheduleAsyncTask(InternalPlugin.INSTANCE, object : AsyncTask() {
                        override fun onRun() {
                            itemFilledMap.renderMap(
                                player.level!!,
                                player.position.floorX - 64,
                                player.position.floorZ - 64,
                                zoom
                            )
                            player.inventory.setItemInHand(itemFilledMap)
                            itemFilledMap.sendImage(player)
                            player.sendMessage("Successfully rendered the map in your hand")
                        }
                    })
                    log.addSuccess("Start rendering the map in your hand. Zoom: $zoom").output()
                    return 1
                }
                return 0
            }

            else -> {
                return 0
            }
        }
    }
}
