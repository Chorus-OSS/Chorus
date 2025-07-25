package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs

class NewJungleTree(
    /**
     * The minimum height of a generated tree.
     */
    private val minTreeHeight: Int, private val maxTreeHeight: Int
) : TreeGenerator() {
    /**
     * The metadata value of the wood to use in tree generation.
     */
    private val metaWood: BlockState =
        BlockJungleLog.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS.createValue(BlockFace.Axis.Y))

    /**
     * The metadata value of the leaves to use in tree generation.
     */
    private val metaLeaves: BlockState = BlockJungleLeaves.properties.defaultState

    override fun generate(level: BlockManager, rand: ChorusRandom, position: Vector3): Boolean {
        val position1 = BlockVector3(position.floorX, position.floorY, position.floorZ)

        val i: Int = rand.nextInt(maxTreeHeight) + this.minTreeHeight
        var flag = true

        //Check the height of the tree and if exist block in it
        if (position1.y >= level.minHeight && position1.y + i + 1 < level.maxHeight) {
            for (j in position1.y..position1.y + 1 + i) {
                var k = 1

                if (j == position1.y) {
                    k = 0
                }

                if (j >= position1.y + 1 + i - 2) {
                    k = 2
                }

                val pos2 = BlockVector3()

                var l = position1.x - k
                while (l <= position1.x + k && flag) {
                    var i1 = position1.z - k
                    while (i1 <= position1.z + k && flag) {
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
                val down = position1.down()
                val block = level.getBlockIdAt(down.x, down.y, down.z)

                if ((block == BlockID.GRASS_BLOCK || block == BlockID.DIRT || block == BlockID.FARMLAND) && position1.y < level.maxHeight - i - 1) {
                    this.setDirtAt(level, down)

                    //Add leaves
                    for (i3 in position1.y - 3 + i..position1.y + i) {
                        val i4 = i3 - (position1.y + i)
                        val j1 = 1 - i4 / 2

                        for (k1 in position1.x - j1..position1.x + j1) {
                            val l1 = k1 - position1.x

                            for (i2 in position1.z - j1..position1.z + j1) {
                                val j2 = i2 - position1.z

                                if (abs(l1) != j1 || abs(j2) != j1 || rand.nextInt(2) != 0 && i4 != 0) {
                                    val blockPos = BlockVector3(k1, i3, i2)
                                    val id = level.getBlockAt(blockPos.x, blockPos.y, blockPos.z)

                                    if (id!!.id == BlockID.AIR || id is BlockLeaves || id.id == BlockID.VINE) {
                                        level.setBlockStateAt(blockPos, metaLeaves)
                                    }
                                }
                            }
                        }
                    }

                    //Add vine
                    for (j3 in 0..<i) {
                        val up = position1.up(j3)
                        val b = level.getBlockAt(up.x, up.y, up.z)
                        val id = b!!.id

                        if (id == BlockID.AIR || b is BlockLeaves || id == BlockID.VINE) {
                            //Add tree trunks
                            level.setBlockStateAt(up, metaWood)
                            if (j3 > 0) {
                                if (rand.nextInt(3) > 0 && isAirBlock(level, position1.add(-1, j3, 0))) {
                                    this.addVine(level, position1.add(-1, j3, 0), 8)
                                }

                                if (rand.nextInt(3) > 0 && isAirBlock(level, position1.add(1, j3, 0))) {
                                    this.addVine(level, position1.add(1, j3, 0), 2)
                                }

                                if (rand.nextInt(3) > 0 && isAirBlock(level, position1.add(0, j3, -1))) {
                                    this.addVine(level, position1.add(0, j3, -1), 1)
                                }

                                if (rand.nextInt(3) > 0 && isAirBlock(level, position1.add(0, j3, 1))) {
                                    this.addVine(level, position1.add(0, j3, 1), 4)
                                }
                            }
                        }
                    }

                    for (k3 in position1.y - 3 + i..position1.y + i) {
                        val j4 = k3 - (position1.y + i)
                        val k4 = 2 - j4 / 2
                        val pos2 = BlockVector3()

                        for (l4 in position1.x - k4..position1.x + k4) {
                            for (i5 in position1.z - k4..position1.z + k4) {
                                pos2.setComponents(l4, k3, i5)

                                if (level.getBlockAt(pos2.x, pos2.y, pos2.z) is BlockLeaves) {
                                    val blockPos1 = pos2.south()
                                    val blockPos2 = pos2.west()
                                    val blockPos3 = pos2.east()
                                    val blockPos4 = pos2.north()

                                    if (rand.nextInt(4) == 0 && level.getBlockIdAt(
                                            blockPos2.x,
                                            blockPos2.y,
                                            blockPos2.z
                                        ) == BlockID.AIR
                                    ) {
                                        this.addHangingVine(level, blockPos2, 8)
                                    }

                                    if (rand.nextInt(4) == 0 && level.getBlockIdAt(
                                            blockPos3.x,
                                            blockPos3.y,
                                            blockPos3.z
                                        ) == BlockID.AIR
                                    ) {
                                        this.addHangingVine(level, blockPos3, 2)
                                    }

                                    if (rand.nextInt(4) == 0 && level.getBlockIdAt(
                                            blockPos4.x,
                                            blockPos4.y,
                                            blockPos4.z
                                        ) == BlockID.AIR
                                    ) {
                                        this.addHangingVine(level, blockPos4, 1)
                                    }

                                    if (rand.nextInt(4) == 0 && level.getBlockIdAt(
                                            blockPos1.x,
                                            blockPos1.y,
                                            blockPos1.z
                                        ) == BlockID.AIR
                                    ) {
                                        this.addHangingVine(level, blockPos1, 4)
                                    }
                                }
                            }
                        }
                    }

                    //Add cocoa beans
                    if (rand.nextInt(5) == 0 && i > 5) {
                        for (l3 in 0..1) {
                            for (enumfacing in BlockFace.Plane.HORIZONTAL_FACES) {
                                if (rand.nextInt(4 - l3) == 0) {
                                    val enumfacing1: BlockFace = enumfacing.getOpposite()
                                    this.placeCocoa(
                                        level,
                                        rand.nextInt(3),
                                        position1.add(enumfacing1.xOffset, i - 5 + l3, enumfacing1.zOffset),
                                        enumfacing
                                    )
                                }
                            }
                        }
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

    private fun placeCocoa(level: BlockManager, age: Int, pos: BlockVector3, side: BlockFace) {
        val blockState: BlockState = BlockCocoa.properties.getBlockState(
            CommonBlockProperties.AGE_3.createValue(age),
            CommonBlockProperties.DIRECTION.createValue(side.horizontalIndex)
        )
        level.setBlockStateAt(pos, blockState)
    }

    private fun addVine(level: BlockManager, pos: BlockVector3, meta: Int) {
        val blockState: BlockState =
            BlockVine.properties.getBlockState(CommonBlockProperties.VINE_DIRECTION_BITS, meta)
        level.setBlockStateAt(pos, blockState)
    }

    private fun addHangingVine(level: BlockManager, pos: BlockVector3, meta: Int) {
        var pos1 = pos
        this.addVine(level, pos1, meta)
        var i = 4

        pos1 = pos1.down()
        while (i > 0 && level.getBlockIdAt(pos1.x, pos1.y, pos1.z) == BlockID.AIR) {
            this.addVine(level, pos1, meta)
            pos1 = pos1.down()
            --i
        }
    }

    private fun isAirBlock(level: BlockManager, v: BlockVector3): Boolean {
        return level.getBlockIdAt(v.x, v.y, v.z) == BlockID.AIR
    }
}
