package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator

class SetBlockCommand(name: String) : VanillaCommand(name, "commands.setblock.description") {
    init {
        this.permission = "chorus.command.setblock"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("position", CommandParamType.POSITION),
            CommandParameter.newEnum("tileName", false, CommandEnum.ENUM_BLOCK),
            CommandParameter.newType("tileData", true, CommandParamType.INT),
            CommandParameter.newEnum(
                "oldBlockHandling",
                true,
                arrayOf("destroy", "keep", "replace")
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
        val locator = list.getResult<Locator>(0)
        var block = list.getResult<Block>(1)
        try {
            if (list.hasResult(2)) {
                val data = list.getResult<Int>(2)!!
                block = block!!.properties.getBlockState(data.toShort())?.toBlock()
            }
        } catch (e: IndexOutOfBoundsException) {
            log.addError("commands.setblock.notFound", block!!.id).output()
            return 0
        }
        var oldBlockHandling: String? = "replace"
        if (list.hasResult(3)) {
            oldBlockHandling = list.getResult(3)
        }
        if (!sender.locator.level.isYInRange(locator!!.position.y.toInt())) {
            log.addError("commands.setblock.outOfWorld").output()
            return 0
        }

        val level = sender.locator.level
        val current = level.getBlock(locator.position)
        if (current.id == block!!.id && current.blockState === block.blockState) {
            log.addError("commands.setblock.noChange").output()
            return 0
        }
        if (current !is BlockAir) {
            when (oldBlockHandling) {
                "destroy" -> {
                    if (sender.isPlayer) {
                        level.useBreakOn(
                            locator.position, null, Item.AIR, sender.asPlayer(),
                            createParticles = true,
                            immediateDestroy = true
                        )
                    } else {
                        level.useBreakOn(locator.position)
                    }
                }

                "keep" -> {
                    log.addError("commands.setblock.noChange").output()
                    return 0
                }
            }
        }
        level.setBlock(locator.position, block)
        log.addSuccess("commands.setblock.success").output()
        return 1
    }
}