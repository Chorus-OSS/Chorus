package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs


open class LegacySpruceTree : LegacyTreeGenerator() {
    override val type: WoodType
        get() = WoodType.SPRUCE

    override fun placeObject(level: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        this.treeHeight = random.nextInt(4) + 6

        val topSize: Int = this.treeHeight - (1 + random.nextInt(2))
        val lRadius: Int = 2 + random.nextInt(2)

        this.placeTrunk(level, x, y, z, random, this.treeHeight - random.nextInt(3))

        this.placeLeaves(level, topSize, lRadius, x, y, z, random)
    }

    open fun placeLeaves(
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
                    val blockAt: Block = level.getBlockAt(xx, yyy, zz) ?: BlockAir()
                    if (!blockAt.isSolid) {
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
