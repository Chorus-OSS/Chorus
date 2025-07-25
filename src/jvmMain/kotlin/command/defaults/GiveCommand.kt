package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockUnknown
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.data.GenericParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.tree.node.RemainStringNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import java.util.stream.Collectors
import kotlin.math.min

class GiveCommand(name: String) : VanillaCommand(name, "commands.give.description") {
    init {
        this.permission = "chorus.command.give"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET, PlayersNode()),
            GenericParameter.Companion.ITEM_NAME.get(false),
            CommandParameter.Companion.newType("amount", true, CommandParamType.INT),
            CommandParameter.Companion.newType("data", true, CommandParamType.INT),
            CommandParameter.Companion.newType("components", true, CommandParamType.JSON, RemainStringNode())
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

        val item = list.getResult<Item>(1)
        if (item!!.isNothing) {
            log.addError("commands.give.item.notFound", item.displayName).output()
            return 0
        }
        if (item is ItemBlock && item.getSafeBlockState().identifier == BlockUnknown.properties.identifier) {
            log.addError("commands.give.block.notFound", item.displayName).output()
            return 0
        }
        val count: Int
        if (list.hasResult(2)) {
            count = list.getResult(2)!!
            if (count <= 0) {
                log.addNumTooSmall(2, 1).output()
                return 0
            }
            item.setCount(count)
        }
        if (list.hasResult(3)) {
            val damage = list.getResult<Int>(3)!!
            item.damage = damage
        }
        if (list.hasResult(4)) {
            val json = list.getResult<String>(4)!!
            val components = Item.ItemJsonComponents.fromJson(json)
            item.readItemJsonComponents(components)
        }

        for (player in players) {
            val returns = player.inventory.addItem(item.clone())
            val drops: MutableList<Item> = ArrayList()
            for (returned in returns) {
                val maxStackSize = returned.maxStackSize
                if (returned.getCount() <= maxStackSize) {
                    drops.add(returned)
                } else {
                    while (returned.getCount() > maxStackSize) {
                        val drop = returned.clone()
                        val toDrop = min(returned.getCount().toDouble(), maxStackSize.toDouble()).toInt()
                        drop.setCount(toDrop)
                        returned.setCount(returned.getCount() - toDrop)
                        drops.add(drop)
                    }
                    if (!returned.isNothing) {
                        drops.add(returned)
                    }
                }
            }

            for (drop in drops) {
                player.dropItem(drop)
            }
            log.outputObjectWhisper(
                player,
                "commands.give.successRecipient",
                item.displayName + " (" + item.id + (if (item.damage != 0) ":" + item.damage else "") + ")",
                item.getCount().toString()
            )
        }
        log.addSuccess(
            "commands.give.success",
            item.displayName + " (" + item.id + (if (item.damage != 0) ":" + item.damage else "") + ")",
            item.getCount().toString(),
            players.stream().map { obj: Player -> obj.getEntityName() }.collect(Collectors.joining(","))
        )
            .successCount(players.size).output(true)
        return players.size
    }
}
