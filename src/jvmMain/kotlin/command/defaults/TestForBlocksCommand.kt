package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Utils
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class TestForBlocksCommand(name: String) : VanillaCommand(name, "commands.testforblocks.description") {
    init {
        this.permission = "chorus.command.testforblocks"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("begin", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("end", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("destination", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("mode", true, arrayOf("all", "masked"))
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
        val begin = list.getResult<Locator>(0)
        val end = list.getResult<Locator>(1)
        val destination = list.getResult<Locator>(2)
        var mode = TestForBlocksMode.ALL

        if (list.hasResult(3)) {
            val str = list.getResult<String>(3)
            mode = TestForBlocksMode.valueOf(str!!.uppercase())
        }

        val blocksAABB: AxisAlignedBB = SimpleAxisAlignedBB(
            min(begin!!.x, end!!.x), min(
                begin.y, end.y
            ), min(begin.z, end.z), max(begin.x, end.x), max(
                begin.y, end.y
            ), max(begin.z, end.z)
        )
        val size =
            floor((blocksAABB.maxX - blocksAABB.minX + 1) * (blocksAABB.maxY - blocksAABB.minY + 1) * (blocksAABB.maxZ - blocksAABB.minZ + 1))

        if (size > 16 * 16 * 256 * 8) {
            log.addError("commands.fill.tooManyBlocks", size.toString(), (16 * 16 * 256 * 8).toString())
            log.addError("Operation will continue, but too many blocks may cause stuttering")
            log.output()
        }

        val to = Vector3(
            destination!!.x + (blocksAABB.maxX - blocksAABB.minX),
            destination.y + (blocksAABB.maxY - blocksAABB.minY),
            destination.z + (blocksAABB.maxZ - blocksAABB.minZ)
        )
        val destinationAABB: AxisAlignedBB = SimpleAxisAlignedBB(
            min(destination.x, to.x), min(
                destination.y, to.y
            ), min(destination.z, to.z), max(
                destination.x, to.x
            ), max(destination.y, to.y), max(
                destination.z, to.z
            )
        )

        if (blocksAABB.minY < 0 || blocksAABB.maxY > 255 || destinationAABB.minY < 0 || destinationAABB.maxY > 255) {
            log.addError("commands.testforblock.outOfWorld").output()
            return 0
        }

        val level = begin.level

        var sourceChunkX = floor(blocksAABB.minX).toInt() shr 4
        var destinationChunkX = floor(destinationAABB.minX).toInt() shr 4
        while (sourceChunkX <= floor(blocksAABB.maxX).toInt() shr 4) {
            var sourceChunkZ = floor(blocksAABB.minZ).toInt() shr 4
            var destinationChunkZ = floor(destinationAABB.minZ).toInt() shr 4
            while (sourceChunkZ <= floor(blocksAABB.maxZ).toInt() shr 4) {
                if (level.getChunkIfLoaded(sourceChunkX, sourceChunkZ) == null) {
                    log.addError("commands.testforblock.outOfWorld").output()
                    return 0
                }
                if (level.getChunkIfLoaded(destinationChunkX, destinationChunkZ) == null) {
                    log.addError("commands.testforblock.outOfWorld").output()
                    return 0
                }
                sourceChunkZ++
                destinationChunkZ++
            }
            sourceChunkX++
            destinationChunkX++
        }

        val blocks = Utils.getLevelBlocks(level, blocksAABB)
        val destinationBlocks = Utils.getLevelBlocks(level, destinationAABB)
        var count = 0

        when (mode) {
            TestForBlocksMode.ALL -> {
                for (i in blocks.indices) {
                    val block = blocks[i]
                    val destinationBlock = destinationBlocks[i]

                    if (block.equalsBlock(destinationBlock)) {
                        ++count
                    } else {
                        log.addError("commands.compare.failed").output()
                        return 0
                    }
                }
            }

            TestForBlocksMode.MASKED -> {
                for (i in blocks.indices) {
                    val block = blocks[i]
                    val destinationBlock = destinationBlocks[i]

                    if (block.equalsBlock(destinationBlock)) {
                        ++count
                    } else if (block.id !== BlockID.AIR) {
                        log.addError("commands.compare.failed").output()
                        return 0
                    }
                }
            }
        }
        log.addSuccess("commands.compare.success", count.toString()).output()
        return 1
    }

    enum class TestForBlocksMode {
        ALL,
        MASKED
    }
}
