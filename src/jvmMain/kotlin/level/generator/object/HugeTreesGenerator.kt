package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockLeaves
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

abstract class HugeTreesGenerator(
    /**
     * The base height of the tree
     */
    protected val baseHeight: Int, protected var extraRandomHeight: Int,
    /**
     * Sets the metadata for the wood blocks used
     */
    protected val woodMetadata: BlockState,
    /**
     * Sets the metadata for the leaves used in huge trees
     */
    protected val leavesMetadata: BlockState
) :
    TreeGenerator() {
    /*
       * Calculates the height based on this trees base height and its extra random height
       */
    protected fun getHeight(rand: ChorusRandom): Int {
        var i: Int = rand.nextInt(3) + this.baseHeight

        if (this.extraRandomHeight > 1) {
            i += rand.nextInt(this.extraRandomHeight)
        }

        return i
    }

    /*
     * returns whether there is space for a tree to grow at a certain position
     */
    private fun isSpaceAt(worldIn: BlockManager, leavesPos: Vector3, height: Int): Boolean {
        var flag = true

        if (leavesPos.y >= 1 && leavesPos.y + height + 1 <= 256) {
            for (i in 0..1 + height) {
                var j = 2

                if (i == 0) {
                    j = 1
                } else if (i >= 1 + height - 2) {
                    j = 2
                }

                var k = -j
                while (k <= j && flag) {
                    var l = -j
                    while (l <= j && flag) {
                        val blockPos = leavesPos.add(k.toDouble(), i.toDouble(), l.toDouble())
                        if (leavesPos.y + i < 0 || leavesPos.y + i >= 256 || !this.canGrowInto(
                                worldIn.getBlockIdAt(
                                    blockPos.x.toInt(),
                                    blockPos.y.toInt(),
                                    blockPos.z.toInt()
                                )
                            )
                        ) {
                            flag = false
                        }
                        ++l
                    }
                    ++k
                }
            }

            return flag
        } else {
            return false
        }
    }

    /*
     * returns whether there is dirt underneath the block where the tree will be grown.
     * It also generates dirt around the block in a 2x2 square if there is dirt underneath the blockpos.
     */
    private fun ensureDirtsUnderneath(pos: Vector3, worldIn: BlockManager): Boolean {
        val blockpos = pos.down()
        val block = worldIn.getBlockIdAt(blockpos.x.toInt(), blockpos.y.toInt(), blockpos.z.toInt())

        if ((block == BlockID.GRASS_BLOCK || block == BlockID.DIRT) && pos.y >= 2) {
            this.setDirtAt(worldIn, blockpos)
            this.setDirtAt(worldIn, blockpos.east())
            this.setDirtAt(worldIn, blockpos.south())
            this.setDirtAt(worldIn, blockpos.south().east())
            return true
        } else {
            return false
        }
    }

    /*
     * returns whether a tree can grow at a specific position.
     * If it can, it generates surrounding dirt underneath.
     */
    protected fun ensureGrowable(
        worldIn: BlockManager,
        rand: ChorusRandom?,
        treePos: Vector3,
        p_175929_4_: Int
    ): Boolean {
        return this.isSpaceAt(worldIn, treePos, p_175929_4_) && this.ensureDirtsUnderneath(treePos, worldIn)
    }

    /*
     * grow leaves in a circle with the outsides being within the circle
     */
    protected fun growLeavesLayerStrict(worldIn: BlockManager, layerCenter: Vector3, width: Int) {
        val i = width * width

        for (j in -width..width + 1) {
            for (k in -width..width + 1) {
                val l = j - 1
                val i1 = k - 1

                if (j * j + k * k <= i || l * l + i1 * i1 <= i || j * j + i1 * i1 <= i || l * l + k * k <= i) {
                    val blockpos = layerCenter.add(j.toDouble(), 0.0, k.toDouble())
                    val block = worldIn.getBlockAt(blockpos.x.toInt(), blockpos.y.toInt(), blockpos.z.toInt())

                    if (block!!.isAir || block is BlockLeaves) {
                        worldIn.setBlockStateAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ, this.leavesMetadata)
                    }
                }
            }
        }
    }

    /*
     * grow leaves in a circle
     */
    protected fun growLeavesLayer(worldIn: BlockManager, layerCenter: Vector3, width: Int) {
        val i = width * width

        for (j in -width..width) {
            for (k in -width..width) {
                if (j * j + k * k <= i) {
                    val blockpos = layerCenter.add(j.toDouble(), 0.0, k.toDouble())
                    val block = worldIn.getBlockAt(blockpos.x.toInt(), blockpos.y.toInt(), blockpos.z.toInt())
                    if (block!!.isAir || block is BlockLeaves) {
                        worldIn.setBlockStateAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ, this.leavesMetadata)
                    }
                }
            }
        }
    }
}