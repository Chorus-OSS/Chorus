package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.data.GenericParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.item.Item
import kotlin.math.min

class ClearCommand(name: String) : VanillaCommand(name, "commands.clear.description", "commands.clear.usage") {
    init {
        this.permission = "chorus.command.clear"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode()),
                GenericParameter.Companion.ITEM_NAME.get(true),
                CommandParameter.Companion.newType("data", true, CommandParamType.INT),
                CommandParameter.Companion.newType("maxCount", true, CommandParamType.INT)
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
        var targets = if (sender.isPlayer) listOf(sender.asPlayer()!!) else null
        var maxCount = -1
        var item: Item? = null

        if (list.hasResult(0)) {
            targets = list.getResult(0)
            if (list.hasResult(1)) {
                item = list.getResult(1)
                var data = -1
                if (list.hasResult(2)) {
                    data = list.getResult(2)!!
                    if (list.hasResult(3)) {
                        maxCount = list.getResult(3)!!
                    }
                }
                item!!.damage = data
            }
        }

        if (targets.isNullOrEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }

        for (target in targets) {
            val inventory = target.inventory
            val offhand = target.offhandInventory

            if (item == null) {
                var count = 0

                for ((key, slot) in inventory.contents.entries) {
                    if (!slot.isNothing) {
                        count += slot.getCount()
                        inventory.clear(key)
                    }
                }

                val slot: Item = offhand.getItem(0)
                if (!slot.isNothing) {
                    count += slot.getCount()
                    offhand.clear(0)
                }

                if (count == 0) {
                    log.addError("commands.clear.failure.no.items", target.getEntityName()).output()
                } else {
                    log.addSuccess("commands.clear.success", target.getEntityName(), count.toString()).output()
                }
            } else if (maxCount == 0) {
                var count = 0

                for ((_, slot) in inventory.contents.entries) {
                    if (item.equals(slot, item.hasMeta(), false)) {
                        count += slot.getCount()
                    }
                }

                val slot: Item = offhand.getItem(0)
                if (item.equals(slot, item.hasMeta(), false)) {
                    count += slot.getCount()
                }

                if (count == 0) {
                    log.addError("commands.clear.failure.no.items", target.getEntityName()).output()
                    return 0
                } else {
                    log.addSuccess("commands.clear.testing", target.getEntityName(), count.toString()).output()
                }
            } else if (maxCount == -1) {
                var count = 0

                for ((key, slot) in inventory.contents.entries) {
                    if (item.equals(slot, item.hasMeta(), false)) {
                        count += slot.getCount()
                        inventory.clear(key)
                    }
                }

                val slot: Item = offhand.getItem(0)
                if (item.equals(slot, item.hasMeta(), false)) {
                    count += slot.getCount()
                    offhand.clear(0)
                }

                if (count == 0) {
                    log.addError("commands.clear.failure.no.items", target.getEntityName()).output()
                    return 0
                } else {
                    log.addSuccess("commands.clear.success", target.getEntityName(), count.toString()).output()
                }
            } else {
                var remaining = maxCount

                for ((key, slot) in inventory.contents.entries) {
                    if (item.equals(slot, item.hasMeta(), false)) {
                        val count = slot.getCount()
                        val amount = min(count.toDouble(), remaining.toDouble()).toInt()

                        slot.setCount(count - amount)
                        inventory.setItem(key, slot)

                        if ((amount.let { remaining -= it; remaining }) <= 0) {
                            break
                        }
                    }
                }

                if (remaining > 0) {
                    val slot: Item = offhand.getItem(0)
                    if (item.equals(slot, item.hasMeta(), false)) {
                        val count = slot.getCount()
                        val amount = min(count.toDouble(), remaining.toDouble()).toInt()

                        slot.setCount(count - amount)
                        inventory.setItem(0, slot)
                        remaining -= amount
                    }
                }

                if (remaining == maxCount) {
                    log.addError("commands.clear.failure.no.items", target.getEntityName()).output()
                    return 0
                } else {
                    log.addSuccess("commands.clear.success", target.getEntityName(), (maxCount - remaining).toString())
                        .output()
                }
            }
        }
        return targets.size
    }
}
