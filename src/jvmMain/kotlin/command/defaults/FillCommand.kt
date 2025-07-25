package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.Utils
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class FillCommand(name: String) : VanillaCommand(name, "commands.fill.description") {
    init {
        this.permission = "chorus.command.fill"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("from", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("to", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("tileName", false, CommandEnum.Companion.ENUM_BLOCK),
                CommandParameter.Companion.newType("blockStates", true, CommandParamType.BLOCK_STATES),
                CommandParameter.Companion.newEnum(
                    "oldBlockHandling",
                    true,
                    arrayOf("destroy", "hollow", "keep", "outline", "replace")
                ),
            )
        )
        this.addCommandParameters(
            "replace", arrayOf(
                CommandParameter.Companion.newType("from", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("to", false, CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("tileName", false, CommandEnum.Companion.ENUM_BLOCK),
                CommandParameter.Companion.newType("blockStates", false, CommandParamType.BLOCK_STATES),
                CommandParameter.Companion.newEnum("oldBlockHandling", false, arrayOf("replace")),
                CommandParameter.Companion.newEnum("replaceTileName", false, CommandEnum.Companion.ENUM_BLOCK),
                CommandParameter.Companion.newType("blockStates", true, CommandParamType.BLOCK_STATES)
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
        val from = list.getResult<Locator>(0)
        val to = list.getResult<Locator>(1)
        val b = list.getResult<Block>(2)
        var tileState = b!!.properties.defaultState
        var oldBlockHandling = FillMode.REPLACE

        val aabb: AxisAlignedBB = SimpleAxisAlignedBB(
            min(from!!.x, to!!.x), min(from.y, to.y), min(
                from.z, to.z
            ), max(from.x, to.x), max(from.y, to.y), max(
                from.z, to.z
            )
        )
        if (aabb.minY < -64 || aabb.maxY > 320) {
            log.addError("commands.fill.outOfWorld").output()
            return 0
        }

        val size =
            floor((aabb.maxX - aabb.minX + 1) * (aabb.maxY - aabb.minY + 1) * (aabb.maxZ - aabb.minZ + 1))
        if (size > 16 * 16 * 16 * 8) {
            log.addError("commands.fill.tooManyBlocks", size.toString(), (16 * 16 * 16 * 8).toString())
            log.addError("Operation will continue, but too many blocks may cause stuttering")
        }

        val level = from.level

        for (chunkX in (floor(aabb.minX).toInt() shr 4)..(floor(aabb.maxX).toInt() shr 4)) {
            for (chunkZ in (floor(aabb.minZ).toInt() shr 4)..(floor(aabb.maxZ).toInt() shr 4)) {
                if (level.getChunkIfLoaded(chunkX, chunkZ) == null) {
                    log.addError("commands.fill.failed").output()
                    return 0
                }
            }
        }
        val blocks: Array<Block>
        var count = 0

        val blockManager: BlockManager = BlockManager(level)
        when (result.key) {
            "default" -> {
                if (list.hasResult(3)) tileState = list.getResult(3)!!
                if (list.hasResult(4)) {
                    val str = list.getResult<String>(4)
                    oldBlockHandling = FillMode.valueOf(str!!.uppercase())
                }
                when (oldBlockHandling) {
                    FillMode.OUTLINE -> {
                        for (x in floor(aabb.minX).toInt()..floor(aabb.maxX).toInt()) {
                            for (z in floor(aabb.minZ).toInt()..floor(aabb.maxZ).toInt()) {
                                for (y in floor(aabb.minY).toInt()..floor(aabb.maxY).toInt()) {
                                    val isBorderX =
                                        x == floor(from.position.x).toInt() || x == floor(
                                            to.position.x
                                        ).toInt()
                                    val isBorderZ =
                                        z == floor(from.position.z).toInt() || z == floor(
                                            to.position.z
                                        ).toInt()
                                    val isBorderY =
                                        y == floor(from.position.y).toInt() || y == floor(
                                            to.position.y
                                        ).toInt()

                                    if (isBorderX || isBorderZ || isBorderY) {
                                        blockManager.setBlockStateAt(x, y, z, tileState)
                                        ++count
                                    }
                                }
                            }
                        }
                    }

                    FillMode.HOLLOW -> {
                        for (x in floor(aabb.minX).toInt()..floor(aabb.maxX).toInt()) {
                            for (z in floor(aabb.minZ).toInt()..floor(aabb.maxZ).toInt()) {
                                for (y in floor(aabb.minY).toInt()..floor(aabb.maxY).toInt()) {
                                    val block: Block
                                    val isBorderX =
                                        x == floor(from.position.x).toInt() || x == floor(
                                            to.position.x
                                        ).toInt()
                                    val isBorderZ =
                                        z == floor(from.position.z).toInt() || z == floor(
                                            to.position.z
                                        ).toInt()
                                    val isBorderY =
                                        y == floor(from.position.y).toInt() || y == floor(
                                            to.position.y
                                        ).toInt()

                                    block = if (isBorderX || isBorderZ || isBorderY) {
                                        tileState.toBlock()
                                    } else {
                                        Block.get(BlockID.AIR)
                                    }

                                    blockManager.setBlockStateAt(x, y, z, block.blockState)
                                    ++count
                                }
                            }
                        }
                    }

                    FillMode.REPLACE -> {
                        blocks = Utils.getLevelBlocks(level, aabb)
                        for (block in blocks) {
                            blockManager.setBlockStateAt(
                                block.position.floorX,
                                block.position.floorY,
                                block.position.floorZ,
                                tileState
                            )
                            ++count
                        }
                    }

                    FillMode.DESTROY -> {
                        blocks = Utils.getLevelBlocks(level, aabb)
                        for (block in blocks) {
                            val players =
                                level.getChunkPlayers(block.position.x.toInt() shr 4, block.position.z.toInt() shr 4)
                            level.addParticle(DestroyBlockParticle(block.position.add(0.5), block), players.values)
                            blockManager.setBlockStateAt(
                                block.position.floorX,
                                block.position.floorY,
                                block.position.floorZ,
                                tileState
                            )
                            ++count
                        }
                    }

                    FillMode.KEEP -> {
                        blocks = Utils.getLevelBlocks(level, aabb)
                        for (block in blocks) {
                            if (block.isAir) {
                                blockManager.setBlockStateAt(
                                    block.position.floorX,
                                    block.position.floorY,
                                    block.position.floorZ,
                                    tileState
                                )
                                ++count
                            }
                        }
                    }
                }
            }

            "replace" -> {
                val replaceBlock = list.getResult<Block>(5)
                val replaceState = if (list.hasResult(6)) {
                    list.getResult(6)!!
                } else {
                    replaceBlock!!.properties.defaultState
                }
                blocks = Utils.getLevelBlocks(level, aabb)
                for (block in blocks) {
                    if (block.id == replaceBlock!!.id) {
                        blockManager.setBlockStateAt(
                            block.position.floorX,
                            block.position.floorY,
                            block.position.floorZ,
                            replaceState
                        )
                        ++count
                    }
                }
            }

            else -> {
                return 0
            }
        }

        if (count == 0) {
            log.addError("commands.fill.failed")
            return 0
        } else {
            blockManager.applySubChunkUpdate()
            log.addSuccess("commands.fill.success", count.toString())
            return 1
        }
    }

    private enum class FillMode {
        REPLACE,
        OUTLINE,
        HOLLOW,
        DESTROY,
        KEEP
    }
}
