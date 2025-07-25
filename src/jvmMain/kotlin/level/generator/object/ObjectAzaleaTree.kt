package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

class ObjectAzaleaTree : TreeGenerator() {
    override fun generate(level: BlockManager, rand: ChorusRandom, position: Vector3): Boolean {
        val i: Int = rand.nextInt(2) + 2
        val j = position.floorX
        val k = position.floorY
        val l = position.floorZ

        val i2 = k + i

        if (k >= -63 && k + i + 2 < 320) {
            val blockPosition = position.down()
            for (il in 0..<i + 1) {
                placeLogAt(level, j, il + k, l)
            }
            this.setDirtAt(level, blockPosition)


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

    override fun setDirtAt(level: BlockManager, pos: Vector3) {
        level.setBlockStateAt(pos.floorX, pos.floorY, pos.floorZ, DIRT_WITH_ROOTS)
    }

    private fun placeLogAt(worldIn: BlockManager, x: Int, y: Int, z: Int) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)

        if (material == BlockID.AIR || material == BlockID.AZALEA_LEAVES || material == BlockID.AZALEA_LEAVES_FLOWERED) {
            worldIn.setBlockStateAt(blockpos, OAK_LOG)
        }
    }

    private fun placeLeafAt(worldIn: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)

        if (material == BlockID.AIR) {
            if (random.nextInt(3) == 1) {
                worldIn.setBlockStateAt(blockpos, AZALEA_LEAVES_FLOWERED)
            } else {
                worldIn.setBlockStateAt(blockpos, AZALEA_LEAVES)
            }
        }
    }

    companion object {
        private val OAK_LOG: BlockState =
            BlockOakLog.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS.createValue(BlockFace.Axis.Y))
        private val DIRT_WITH_ROOTS: BlockState = BlockDirtWithRoots.properties.defaultState
        private val AZALEA_LEAVES_FLOWERED: BlockState = BlockAzaleaLeavesFlowered.properties.defaultState
        private val AZALEA_LEAVES: BlockState = BlockAzaleaLeaves.properties.defaultState
    }
}