package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import java.util.*

abstract class TreeGenerator : ObjectGenerator() {
    /*
    * returns whether a tree can grow into a block
    * For example, a tree will not grow into stone
    */
    protected fun canGrowInto(id: String): Boolean {
        return when (id) {
            BlockID.AIR,
            BlockID.ACACIA_LEAVES,
            BlockID.AZALEA_LEAVES,
            BlockID.BIRCH_LEAVES,
            BlockID.AZALEA_LEAVES_FLOWERED,
            BlockID.CHERRY_LEAVES,
            BlockID.DARK_OAK_LEAVES,
            BlockID.JUNGLE_LEAVES,
            BlockID.MANGROVE_LEAVES,
            BlockID.OAK_LEAVES,
            BlockID.SPRUCE_LEAVES,
            BlockID.PALE_OAK_LEAVES,
            BlockID.GRASS_BLOCK,
            BlockID.DIRT,
            BlockID.ACACIA_LOG,
            BlockID.BIRCH_LOG,
            BlockID.OAK_LOG,
            BlockID.PALE_OAK_LOG,
            BlockID.DARK_OAK_LOG,
            BlockID.JUNGLE_LOG,
            BlockID.MANGROVE_LOG,
            BlockID.SPRUCE_LOG,
            BlockID.VINE,
            BlockID.DIRT_WITH_ROOTS,
            BlockID.CHERRY_LOG,
            BlockID.MANGROVE_ROOTS,
            BlockID.MANGROVE_PROPAGULE,
            BlockID.ACACIA_SAPLING,
            BlockID.CHERRY_SAPLING,
            BlockID.SPRUCE_SAPLING,
            BlockID.BAMBOO_SAPLING,
            BlockID.OAK_SAPLING,
            BlockID.JUNGLE_SAPLING,
            BlockID.DARK_OAK_SAPLING,
            BlockID.PALE_OAK_SAPLING,
            BlockID.BIRCH_SAPLING -> true

            else -> false
        }
    }

    fun generateSaplings(level: BlockManager?, random: Random?, pos: Vector3?) {}

    protected fun setDirtAt(level: BlockManager, pos: BlockVector3) {
        setDirtAt(level, Vector3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
    }

    /*
     * sets dirt at a specific location if it isn't already dirt
     */
    protected open fun setDirtAt(level: BlockManager, pos: Vector3) {
        level.setBlockStateAt(pos.floorX, pos.floorY, pos.floorZ, BlockID.DIRT)
    }
}

