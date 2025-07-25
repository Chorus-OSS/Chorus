package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Locator

class TestForBlockCommand(name: String) : VanillaCommand(name, "commands.testforblock.description") {
    init {
        this.permission = "chorus.command.testforblock"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("position", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("tileName", false, CommandEnum.Companion.ENUM_BLOCK),
                CommandParameter.Companion.newType("dataValue", true, CommandParamType.INT)
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
        val tileName = list.getResult<Block>(1)
        val tileId = tileName!!.id
        var dataValue = 0
        if (list.hasResult(2)) {
            dataValue = list.getResult(2)!!
        }
        if (!Block.get(tileId).properties.containBlockState(dataValue.toShort())) {
            log.addError("commands.give.block.notFound", tileId).output()
            return 0
        }

        val level = locator!!.level

        if (level.getChunkIfLoaded(locator.position.chunkX, locator.position.chunkZ) == null) {
            log.addError("commands.testforblock.outOfWorld").output()
            return 0
        }

        val block = level.getBlock(locator.position, false)
        val id = block.id
        val meta = block.blockState.specialValue().toInt()

        if (id === tileId && meta == dataValue) {
            log.addSuccess(
                "commands.testforblock.success",
                locator.position.floorX.toString(),
                locator.position.floorY.toString(),
                locator.position.floorZ.toString()
            ).output()
            return 1
        } else {
            log.addError(
                "commands.testforblock.failed.tile",
                locator.position.floorX.toString(),
                locator.position.floorY.toString(),
                locator.position.floorZ.toString(),
                id.toString(),
                tileId.toString()
            )
                .output()
            return 0
        }
    }
}
