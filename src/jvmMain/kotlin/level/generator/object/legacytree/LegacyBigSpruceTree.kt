package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs

class LegacyBigSpruceTree(private val leafStartHeightMultiplier: Float, private val baseLeafRadius: Int) :
    LegacySpruceTree() {
    fun setRandomTreeHeight(random: ChorusRandom) {
        this.treeHeight = random.nextInt(15) + 20
    }

    override fun placeObject(level: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        if (this.treeHeight == 0) {
            this.setRandomTreeHeight(random)
        }

        val topSize = this.treeHeight - (this.treeHeight * leafStartHeightMultiplier).toInt()
        val lRadius: Int = baseLeafRadius + random.nextInt(2)

        this.placeTrunk(level, x, y, z, random, this.treeHeight - random.nextInt(3))

        this.placeLeaves(level, topSize, lRadius, x, y, z, random)
    }

    override fun placeTrunk(
        level: BlockManager,
        x: Int,
        y: Int,
        z: Int,
        random: ChorusRandom?,
        trunkHeight: Int
    ) {
        // The base dirt block
        level.setBlockStateAt(x, y - 1, z, BlockID.DIRT)
        val radius = 2

        for (yy in 0..<trunkHeight) {
            for (xx in 0..<radius) {
                for (zz in 0..<radius) {
                    val b = level.getBlockAt(x + xx, y + yy, z + zz) ?: BlockAir()
                    if (this.overridable(b)) {
                        level.setBlockStateAt(x + xx, y + yy, z + zz, trunkBlockState)
                    }
                }
            }
        }
    }

    override fun placeLeaves(
        level: BlockManager,
        topSize: Int,
        lRadius: Int,
        x: Int,
        y: Int,
        z: Int,
        random: ChorusRandom
    ) {
        var radius: Int = random.nextInt(2)
        var maxR = 1
        var minR = 0

        for (yy in 0..topSize) {
            val yyy = y + this.treeHeight - yy

            for (xx in x - radius..x + radius) {
                val xOff = abs(xx - x)
                for (zz in z - radius..z + radius) {
                    val zOff = abs(zz - z)
                    if (xOff == radius && zOff == radius && radius > 0) {
                        continue
                    }
                    val solid = level.getBlockAt(xx, yyy, zz)?.isSolid ?: false
                    if (!solid) {
                        level.setBlockStateAt(xx, yyy, zz, leafBlockState)
                    }
                }
            }

            if (radius >= maxR) {
                radius = minR
                minR = 1
                if (++maxR > lRadius) {
                    maxR = lRadius
                }
            } else {
                ++radius
            }
        }
    }
}
