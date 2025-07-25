package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.block.Block
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

class CloneCommand(name: String) : VanillaCommand(name, "commands.clone.description") {
    init {
        this.permission = "chorus.command.clone"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("begin", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("end", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("destination", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("maskMode", true, arrayOf("masked", "replace")),
                CommandParameter.Companion.newEnum("cloneMode", true, arrayOf("force", "move", "normal"))
            )
        )
        this.addCommandParameters(
            "filtered", arrayOf(
                CommandParameter.Companion.newType("begin", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("end", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("destination", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("maskMode", false, arrayOf("filtered")),
                CommandParameter.Companion.newEnum("cloneMode", false, arrayOf("force", "move", "normal")),
                CommandParameter.Companion.newType("tileId", false, CommandParamType.INT),
                CommandParameter.Companion.newType("tileData", false, CommandParamType.INT)
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
        var maskMode = MaskMode.REPLACE
        var cloneMode = CloneMode.NORMAL
        var tileId: String? = BlockID.AIR
        var tileData = 0
        when (result.key) {
            "default" -> {
                if (list.hasResult(3)) {
                    val str = list.getResult<String>(3)
                    maskMode = MaskMode.valueOf(str!!.uppercase())
                }
                if (list.hasResult(4)) {
                    val str = list.getResult<String>(4)
                    cloneMode = CloneMode.valueOf(str!!.uppercase())
                }
            }

            "filtered" -> {
                maskMode = MaskMode.FILTERED
                val str = list.getResult<String>(4)
                cloneMode = CloneMode.valueOf(str!!.uppercase())
                tileId = list.getResult(5)
                tileData = list.getResult(6)!!
            }

            else -> {
                return 0
            }
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
            log.addError("commands.clone.tooManyBlocks", size.toString(), (16 * 16 * 256 * 8).toString())
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

        if (blocksAABB.minY < -64 || blocksAABB.maxY > 320 || destinationAABB.minY < -64 || destinationAABB.maxY > 320) {
            log.addOutOfWorld().output()
            return 0
        }
        if (blocksAABB.intersectsWith(destinationAABB) && cloneMode != CloneMode.FORCE) {
            log.addError("commands.clone.noOverlap").output()
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
                    log.addOutOfWorld().output()
                    return 0
                }
                if (level.getChunkIfLoaded(destinationChunkX, destinationChunkZ) == null) {
                    log.addOutOfWorld().output()
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

        val move = cloneMode == CloneMode.MOVE
        when (maskMode) {
            MaskMode.REPLACE -> {
                for (i in blocks.indices) {
                    val block = blocks[i]
                    val destinationBlock = destinationBlocks[i]

                    block.cloneTo(destinationBlock)

                    ++count

                    if (move) {
                        level.setBlock(block.position, Block.get(BlockID.AIR))
                    }
                }
            }

            MaskMode.MASKED -> {
                for (i in blocks.indices) {
                    val block = blocks[i]
                    val destinationBlock = destinationBlocks[i]

                    if (block.id !== BlockID.AIR) {
                        block.cloneTo(destinationBlock)
                        ++count

                        if (move) {
                            level.setBlock(block.position, Block.get(BlockID.AIR))
                        }
                    }
                }
            }

            MaskMode.FILTERED -> {
                for (i in blocks.indices) {
                    val block = blocks[i]
                    val destinationBlock = destinationBlocks[i]

                    if (block.id === tileId && block.blockState.specialValue().toInt() == tileData) {
                        block.cloneTo(destinationBlock)
                        ++count

                        if (move) {
                            level.setBlock(block.position, Block.get(BlockID.AIR))
                        }
                    }
                }
            }
        }

        if (count == 0) {
            log.addError("commands.clone.failed").output()
        } else {
            log.addSuccess("commands.clone.success", count.toString()).output()
        }
        return 1
    }

    private enum class MaskMode {
        REPLACE,
        MASKED,
        FILTERED
    }

    private enum class CloneMode {
        NORMAL,
        FORCE,
        MOVE
    }
}
