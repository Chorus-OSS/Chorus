package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs

class ObjectSmallPaleOakTree(
    /**
     * The minimum height of a generated tree.
     */
    private val minTreeHeight: Int, private val maxTreeHeight: Int
) : TreeGenerator() {
    /**
     * The metadata value of the wood to use in tree generation.
     */
    private val metaWood: BlockState =
        BlockPaleOakWood.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS.createValue(BlockFace.Axis.Y))

    /**
     * The metadata value of the leaves to use in tree generation.
     */
    private val metaLeaves: BlockState = BlockPaleOakLeaves.properties.defaultState

    override fun generate(level: BlockManager, rand: ChorusRandom, vectorPosition: Vector3): Boolean {
        val position = BlockVector3(vectorPosition.floorX, vectorPosition.floorY, vectorPosition.floorZ)

        var i: Int = rand.nextInt(maxTreeHeight) + this.minTreeHeight
        var flag = true

        //Check the height of the tree and if exist block in it
        if (position.y >= level.minHeight && position.y + i + 1 < level.maxHeight) {
            for (j in position.y..position.y + 1 + i) {
                var k = 1

                if (j == position.y) {
                    k = 0
                }

                if (j >= position.y + 1 + i - 2) {
                    k = 2
                }

                val pos2 = BlockVector3()

                var l = position.x - k
                while (l <= position.x + k && flag) {
                    var i1 = position.z - k
                    while (i1 <= position.z + k && flag) {
                        if (j >= level.minHeight && j < level.maxHeight) {
                            pos2.setComponents(l, j, i1)
                            if (!this.canGrowInto(level.getBlockIdAt(pos2.x, pos2.y, pos2.z))) {
                                flag = false
                            }
                        } else {
                            flag = false
                        }
                        ++i1
                    }
                    ++l
                }
            }

            if (!flag) {
                return false
            } else {
                val down = position.down()
                val block = level.getBlockIdAt(down.x, down.y, down.z)

                if ((block == BlockID.GRASS_BLOCK || block == BlockID.DIRT || block == BlockID.FARMLAND) && position.y < level.maxHeight - i - 1) {
                    this.setDirtAt(level, down)

                    //Add leaves
                    var i3 = position.y - 3 + i
                    while (i3 <= position.y + i) {
                        val i4 = i3 - (position.y + i)
                        val j1 = 1 - i4 / 2

                        for (k1 in position.x - j1..position.x + j1) {
                            val l1 = k1 - position.x

                            for (i2 in position.z - j1..position.z + j1) {
                                val j2 = i2 - position.z

                                if (abs(l1) != j1 || abs(j2) != j1 || rand.nextInt(2) != 0 && i4 != 0) {
                                    val blockpos = BlockVector3(k1, i3, i2)
                                    val id = level.getBlockAt(blockpos.x, blockpos.y, blockpos.z)

                                    if (id!!.id == BlockID.AIR || id is BlockLeaves || id.id == BlockID.PALE_HANGING_MOSS) {
                                        level.setBlockStateAt(blockpos, metaLeaves)
                                        val random = ChorusRandom(level.seed + blockpos.x + blockpos.y + blockpos.z)
                                        if (random.nextInt(2) == 0) {
                                            val depth = random.nextInt(1, 6)
                                            val j = 1
                                            while (j < depth) {
                                                val pos = Vector3(
                                                    blockpos.x.toDouble(),
                                                    (blockpos.y - i).toDouble(),
                                                    blockpos.z.toDouble()
                                                )
                                                if (level.getBlockAt(pos)!!.isAir) {
                                                    if (i == depth - 1) {
                                                        level.setBlockStateAt(
                                                            pos,
                                                            BlockPaleHangingMoss.properties.getBlockState(
                                                                CommonBlockProperties.TIP,
                                                                true
                                                            )
                                                        )
                                                    } else level.setBlockStateAt(
                                                        pos,
                                                        BlockPaleHangingMoss.properties.getBlockState(
                                                            CommonBlockProperties.TIP,
                                                            false
                                                        )
                                                    )
                                                } else break
                                                i++
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ++i3
                    }
                    return true
                } else {
                    return false
                }
            }
        } else {
            return false
        }
    }
}
