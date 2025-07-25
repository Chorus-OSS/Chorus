package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

class ObjectMangroveTree : TreeGenerator() {
    override fun generate(level: BlockManager, rand: ChorusRandom, position: Vector3): Boolean {
        val i: Int = rand.nextInt(3) + 8
        val j = position.floorX
        val k = position.floorY
        val l = position.floorZ

        val i2 = k + i

        if (k >= -63 && k + i + 2 < 320) {
            for (il in 0..<i + 1) {
                if (il > 2) {
                    placeLogAt(level, j, il + k, l)
                } else {
                    placeRootAt(level, j + 1, il + k, l)
                    placeRootAt(level, j - 1, il + k, l)
                    placeRootAt(level, j, il + k, l + 1)
                    placeRootAt(level, j, il + k, l - 1)
                }
            }
            placeRootAt(level, j + 2, 0 + k, l)
            placeRootAt(level, j - 2, 0 + k, l)
            placeRootAt(level, j, 0 + k, l + 2)
            placeRootAt(level, j, 0 + k, l - 2)
            for (i3 in -2..1) {
                for (l3 in -2..1) {
                    var k4 = 1
                    var offsetX: Int = rand.nextInt(0, 1)
                    var offsetY: Int = rand.nextInt(0, 1)
                    var offsetZ: Int = rand.nextInt(0, 1)
                    this.placeLeafAt(level, j + i3 + offsetX, i2 + k4 + offsetY, l + l3 + offsetZ, rand)
                    this.placeLeafAt(level, j - i3 + offsetX, i2 + k4 + offsetY, l + l3 + offsetZ, rand)
                    this.placeLeafAt(level, j + i3 + offsetX, i2 + k4 + offsetY, l - l3 + offsetZ, rand)
                    this.placeLeafAt(level, j - i3 + offsetX, i2 + k4 + offsetY, l - l3 + offsetZ, rand)

                    k4 = 0
                    this.placeLeafAt(level, j + i3, i2 + k4, l + l3, rand)
                    this.placeLeafAt(level, j - i3, i2 + k4, l + l3, rand)
                    this.placeLeafAt(level, j + i3, i2 + k4, l - l3, rand)
                    this.placeLeafAt(level, j - i3, i2 + k4, l - l3, rand)

                    k4 = 1
                    this.placeLeafAt(level, j + i3, i2 + k4, l + l3, rand)
                    this.placeLeafAt(level, j - i3, i2 + k4, l + l3, rand)
                    this.placeLeafAt(level, j + i3, i2 + k4, l - l3, rand)
                    this.placeLeafAt(level, j - i3, i2 + k4, l - l3, rand)

                    k4 = 2
                    offsetX = rand.nextInt(-1, 0)
                    offsetY = rand.nextInt(-1, 0)
                    offsetZ = rand.nextInt(-1, 0)

                    this.placeLeafAt(level, j + i3 + offsetX, i2 + k4 + offsetY, l + l3 + offsetZ, rand)
                    this.placeLeafAt(level, j - i3 + offsetX, i2 + k4 + offsetY, l + l3 + offsetZ, rand)
                    this.placeLeafAt(level, j + i3 + offsetX, i2 + k4 + offsetY, l - l3 + offsetZ, rand)
                    this.placeLeafAt(level, j - i3 + offsetX, i2 + k4 + offsetY, l - l3 + offsetZ, rand)
                }
            }
            return true
        }

        return false
    }

    private fun placeLogAt(worldIn: BlockManager, x: Int, y: Int, z: Int) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
        if (material === BlockID.AIR || material === BlockID.MANGROVE_LEAVES || material === BlockID.MANGROVE_PROPAGULE) {
            worldIn.setBlockStateAt(blockpos, LOG)
        }
    }

    private fun placeRootAt(worldIn: BlockManager, x: Int, y: Int, z: Int) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
        if (material === BlockID.AIR || material === BlockID.MANGROVE_LEAVES || material === BlockID.MANGROVE_PROPAGULE) {
            worldIn.setBlockStateAt(blockpos, ROOTS)
        }
    }

    private fun placeLeafAt(worldIn: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
        if (material!!.isAir || (material is BlockMangrovePropagule && material.isHanging)) {
            worldIn.setBlockStateAt(blockpos, MANGROVE_LEAVES)
            if (random.nextInt(7) == 0) {
                placePropaguleAt(worldIn, blockpos.floorX, blockpos.floorY - 1, blockpos.floorZ)
            }
        }
    }

    private fun placePropaguleAt(worldIn: BlockManager, x: Int, y: Int, z: Int) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
        if (material === BlockID.AIR) {
            //Todo: Fix hanging mangrove propagule
        }
    }

    companion object {
        private val LOG: BlockState =
            BlockMangroveLog.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS.createValue(BlockFace.Axis.Y))
        private val ROOTS: BlockState = BlockMangroveRoots.properties.defaultState
        private val MANGROVE_LEAVES: BlockState = BlockMangroveLeaves.properties.defaultState
    }
}
