package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.BlockVine
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.cos
import kotlin.math.sin

class ObjectJungleBigTree(
    baseHeightIn: Int,
    extraRandomHeight: Int,
    woodMetadata: BlockState,
    leavesMetadata: BlockState
) :
    HugeTreesGenerator(baseHeightIn, extraRandomHeight, woodMetadata, leavesMetadata) {
    override fun generate(level: BlockManager, rand: ChorusRandom, position: Vector3): Boolean {
        val height = this.getHeight(rand)

        if (!this.ensureGrowable(level, rand, position, height)) {
            return false
        } else {
            this.createCrown(level, position.up(height), 2)

            var j: Int = position.y.toInt() + height - 2 - rand.nextInt(4)
            while (j > position.y + height.toDouble() / 2) {
                val f: Float = rand.nextFloat() * (Math.PI.toFloat() * 2f)
                var k = (position.x + (0.5f + cos(f) * 4.0f)).toInt()
                var l = (position.z + (0.5f + sin(f) * 4.0f)).toInt()

                for (i1 in 0..4) {
                    k = (position.x + (1.5f + cos(f) * i1.toFloat())).toInt()
                    l = (position.z + (1.5f + sin(f) * i1.toFloat())).toInt()
                    level.setBlockStateAt(BlockVector3(k, j - 3 + i1 / 2, l), this.woodMetadata)
                }

                val j2: Int = 1 + rand.nextInt(2)

                for (k1 in j - j2..j) {
                    val l1 = k1 - j
                    this.growLeavesLayer(level, Vector3(k.toDouble(), k1.toDouble(), l.toDouble()), 1 - l1)
                }
                j -= 2 + rand.nextInt(4)
            }

            for (i2 in 0..<height) {
                val blockpos = position.up(i2)

                if (this.canGrowInto(level.getBlockIdAt(blockpos.x.toInt(), blockpos.y.toInt(), blockpos.z.toInt()))) {
                    level.setBlockStateAt(blockpos.x.toInt(), blockpos.y.toInt(), blockpos.z.toInt(), this.woodMetadata)
                    if (i2 > 0) {
                        this.placeVine(level, rand, blockpos.west(), 8)
                        this.placeVine(level, rand, blockpos.north(), 1)
                    }
                }

                if (i2 < height - 1) {
                    val blockpos1 = blockpos.east()

                    if (this.canGrowInto(
                            level.getBlockIdAt(
                                blockpos1.x.toInt(),
                                blockpos1.y.toInt(),
                                blockpos1.z.toInt()
                            )
                        )
                    ) {
                        level.setBlockStateAt(
                            blockpos1.x.toInt(),
                            blockpos1.y.toInt(),
                            blockpos1.z.toInt(),
                            this.woodMetadata
                        )

                        if (i2 > 0) {
                            this.placeVine(level, rand, blockpos1.east(), 2)
                            this.placeVine(level, rand, blockpos1.north(), 1)
                        }
                    }

                    val blockpos2 = blockpos.south().east()

                    if (this.canGrowInto(
                            level.getBlockIdAt(
                                blockpos2.x.toInt(),
                                blockpos2.y.toInt(),
                                blockpos2.z.toInt()
                            )
                        )
                    ) {
                        level.setBlockStateAt(
                            blockpos2.x.toInt(),
                            blockpos2.y.toInt(),
                            blockpos2.z.toInt(),
                            this.woodMetadata
                        )

                        if (i2 > 0) {
                            this.placeVine(level, rand, blockpos2.east(), 2)
                            this.placeVine(level, rand, blockpos2.south(), 4)
                        }
                    }

                    val blockpos3 = blockpos.south()

                    if (this.canGrowInto(
                            level.getBlockIdAt(
                                blockpos3.x.toInt(),
                                blockpos3.y.toInt(),
                                blockpos3.z.toInt()
                            )
                        )
                    ) {
                        level.setBlockStateAt(
                            blockpos3.x.toInt(),
                            blockpos3.y.toInt(),
                            blockpos3.z.toInt(),
                            this.woodMetadata
                        )

                        if (i2 > 0) {
                            this.placeVine(level, rand, blockpos3.west(), 8)
                            this.placeVine(level, rand, blockpos3.south(), 4)
                        }
                    }
                }
            }

            return true
        }
    }

    private fun placeVine(level: BlockManager, random: ChorusRandom, pos: Vector3, meta: Int) {
        if (random.nextInt(3) > 0 && level.getBlockIdAt(pos.x.toInt(), pos.y.toInt(), pos.z.toInt()) == BlockID.AIR) {
            val block: BlockState = BlockVine.properties.getBlockState(
                CommonBlockProperties.VINE_DIRECTION_BITS,
                meta
            )
            level.setBlockStateAt(pos, block)
        }
    }

    private fun createCrown(level: BlockManager, pos: Vector3, i1: Int) {
        for (j in -2..0) {
            this.growLeavesLayerStrict(level, pos.up(j), i1 + 1 - j)
        }
    }
}

