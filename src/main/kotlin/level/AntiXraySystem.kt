package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.registry.Registries

class AntiXraySystem(private val level: Level) {
    var fakeOreDenominator: Int = 16
    var isPreDeObfuscate: Boolean = true
    val rawRealOreToReplacedRuntimeIdMap: MutableMap<Int, Int> = mutableMapOf()
    private val fakeOreToPutRuntimeIds: MutableMap<Int, MutableList<Int>> = mutableMapOf()

    fun addAntiXrayOreBlock(oreBlock: Block, replaceWith: Block) {
        rawRealOreToReplacedRuntimeIdMap.put(oreBlock.runtimeId, replaceWith.runtimeId)
    }

    fun removeAntiXrayOreBlock(oreBlock: Block, replaceWith: Block) {
        rawRealOreToReplacedRuntimeIdMap.remove(oreBlock.runtimeId, replaceWith.runtimeId)
    }

    fun addAntiXrayFakeBlock(originBlock: Block, fakeBlocks: Collection<Block>) {
        val rid = originBlock.runtimeId
        var list = fakeOreToPutRuntimeIds[rid]
        if (list == null) {
            fakeOreToPutRuntimeIds[rid] = mutableListOf<Int>().also { list = it }
        }
        for (each in fakeBlocks) {
            list!!.add(each.runtimeId)
        }
    }

    fun removeAntiXrayFakeBlock(originBlock: Block, fakeBlocks: Collection<Block>) {
        val rid = originBlock.runtimeId
        val list = fakeOreToPutRuntimeIds[rid]
        if (list != null) {
            for (each in fakeBlocks) {
                val tmp = each.runtimeId
                list.removeIf { it == tmp }
            }
        }
    }

    val rawFakeOreToPutRuntimeIdMap: MutableMap<Int, MutableList<Int>>
        get() = this.fakeOreToPutRuntimeIds

    fun obfuscateSendBlocks(index: Long, playerArray: Array<Player>, blocks: Map<Int, Any>) {
        val size: Int = blocks.size
        val vectorSet: MutableSet<Int> = mutableSetOf()
        val vRidList = ArrayList<Vector3WithRuntimeId>(size * 7)
        var tmpV3Rid: Vector3WithRuntimeId
        for (blockHash in blocks.keys.iterator()) {
            var blockHash1 = blockHash
            val hash: Vector3 = Level.getBlockXYZ(index, blockHash1, level)
            var x = hash.floorX
            var y = hash.floorY
            var z = hash.floorZ
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    tmpV3Rid = Vector3WithRuntimeId(
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble(),
                        level.getBlockRuntimeId(x, y, z, 0),
                        level.getBlockRuntimeId(x, y, z, 1)
                    )
                    vRidList.add(tmpV3Rid)
                    if (!transparentBlockRuntimeIds.contains(tmpV3Rid.runtimeIdLayer0)) {
                        continue
                    }
                } catch (ignore: Exception) {
                }
            }
            x++
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
            x -= 2
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
            x++
            y++
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
            y -= 2
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
            y++
            z++
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
            z -= 2
            blockHash1 = Level.localBlockHash(x, y, z, 0, level)
            if (!vectorSet.contains(blockHash1)) {
                vectorSet.add(blockHash1)
                try {
                    vRidList.add(
                        Vector3WithRuntimeId(
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            level.getBlockRuntimeId(x, y, z, 0),
                            level.getBlockRuntimeId(x, y, z, 1)
                        )
                    )
                } catch (ignore: Exception) {
                }
            }
        }
        level.sendBlocks(
            playerArray,
            vRidList.toTypedArray(),
            org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL.toInt()
        )
    }

    fun deObfuscateBlock(player: Player, face: BlockFace, target: Block) {
        val vecList = ArrayList<Vector3WithRuntimeId>(5)
        var tmpVec: Vector3WithRuntimeId
        for (each in BlockFace.entries) {
            if (each == face) continue
            val tmpX: Int = target.position.floorX + each.xOffset
            val tmpY: Int = target.position.floorY + each.yOffset
            val tmpZ: Int = target.position.floorZ + each.zOffset
            try {
                tmpVec = Vector3WithRuntimeId(
                    tmpX.toDouble(),
                    tmpY.toDouble(),
                    tmpZ.toDouble(),
                    level.getBlockRuntimeId(tmpX, tmpY, tmpZ, 0),
                    level.getBlockRuntimeId(tmpX, tmpY, tmpZ, 1)
                )
                if (rawFakeOreToPutRuntimeIdMap.containsKey(tmpVec.runtimeIdLayer0)) {
                    vecList.add(tmpVec)
                }
            } catch (ignore: Exception) {
            }
        }
        level.sendBlocks(
            arrayOf(player),
            vecList.toTypedArray(),
            org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL.toInt()
        )
    }

    fun reinitAntiXray(global: Boolean) {
        val stone = BlockStone()
        val netherrack = BlockNetherrack()
        val deepslate = BlockDeepslate()
        run {
            rawFakeOreToPutRuntimeIdMap.clear()
            rawRealOreToReplacedRuntimeIdMap.clear()
        }
        run {
            addAntiXrayOreBlock(BlockCoalOre(), stone)
            addAntiXrayOreBlock(BlockDiamondOre(), stone)
            addAntiXrayOreBlock(BlockEmeraldOre(), stone)
            addAntiXrayOreBlock(BlockGoldOre(), stone)
            addAntiXrayOreBlock(BlockIronOre(), stone)
            addAntiXrayOreBlock(BlockLapisOre(), stone)
            addAntiXrayOreBlock(BlockRedstoneOre(), stone)
            addAntiXrayOreBlock(BlockQuartzOre(), netherrack)
            addAntiXrayOreBlock(BlockNetherGoldOre(), netherrack)
            addAntiXrayOreBlock(BlockAncientDebris(), netherrack)
            addAntiXrayOreBlock(BlockDeepslateCoalOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateDiamondOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateEmeraldOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateGoldOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateIronOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateLapisOre(), deepslate)
            addAntiXrayOreBlock(BlockDeepslateRedstoneOre(), deepslate)
        }
        run {
            addAntiXrayFakeBlock(
                stone,
                listOf(
                    BlockCoalOre(),
                    BlockDiamondOre(),
                    BlockEmeraldOre(),
                    BlockGoldOre(),
                    BlockIronOre(),
                    BlockLapisOre(),
                    BlockRedstoneOre()
                )
            )
            addAntiXrayFakeBlock(
                netherrack,
                listOf(BlockQuartzOre(), BlockNetherGoldOre(), BlockAncientDebris())
            )
            addAntiXrayFakeBlock(
                deepslate,
                listOf(
                    BlockDeepslateCoalOre(),
                    BlockDeepslateDiamondOre(),
                    BlockDeepslateEmeraldOre(),
                    BlockDeepslateGoldOre(),
                    BlockDeepslateIronOre(),
                    BlockDeepslateLapisOre(),
                    BlockDeepslateRedstoneOre()
                )
            )
        }
        if (global || transparentBlockRuntimeIds.isEmpty()) {
            transparentBlockRuntimeIds.clear()
            for (each in Registries.BLOCKSTATE.allState) {
                try {
                    val block = Block.get(each)
                    if (block.isTransparent) {
                        transparentBlockRuntimeIds.add(block.runtimeId)
                    }
                } catch (ignore: Exception) {
                }
            }
        }
    }

    companion object {
        private val transparentBlockRuntimeIds: MutableSet<Int> = mutableSetOf(256)
        val rawTransparentBlockRuntimeIds: MutableSet<Int>
            get() = transparentBlockRuntimeIds
    }
}
