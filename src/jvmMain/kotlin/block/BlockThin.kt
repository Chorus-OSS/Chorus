package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.VectorMath.calculateFace
import org.chorus_oss.chorus.utils.LevelException

abstract class BlockThin(blockState: BlockState) : BlockTransparent(blockState), BlockConnectable {
    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val offNW = 7.0 / 16.0
        val offSE = 9.0 / 16.0
        val onNW = 0.0
        val onSE = 1.0
        var w = offNW
        var e = offSE
        var n = offNW
        var s = offSE
        try {
            val north = this.canConnect(this.north())
            val south = this.canConnect(this.south())
            val west = this.canConnect(this.west())
            val east = this.canConnect(this.east())
            w = if (west) onNW else offNW
            e = if (east) onSE else offSE
            n = if (north) onNW else offNW
            s = if (south) onSE else offSE
        } catch (ignore: LevelException) {
            //null sucks
        }
        return SimpleAxisAlignedBB(
            position.x + w,
            position.y,
            position.z + n,
            position.x + e,
            position.y + 1,
            position.z + s
        )
    }

    override fun canConnect(block: Block?): Boolean {
        if (block != null) {
            return when (block.id) {
                BlockID.GLASS_PANE, BlockID.BLACK_STAINED_GLASS_PANE, BlockID.BLUE_STAINED_GLASS_PANE, BlockID.BROWN_STAINED_GLASS_PANE, BlockID.CYAN_STAINED_GLASS_PANE, BlockID.GRAY_STAINED_GLASS_PANE, BlockID.GREEN_STAINED_GLASS_PANE, BlockID.LIGHT_BLUE_STAINED_GLASS_PANE, BlockID.LIGHT_GRAY_STAINED_GLASS_PANE, BlockID.LIME_STAINED_GLASS_PANE, BlockID.MAGENTA_STAINED_GLASS_PANE, BlockID.ORANGE_STAINED_GLASS_PANE, BlockID.PINK_STAINED_GLASS_PANE, BlockID.PURPLE_STAINED_GLASS_PANE, BlockID.RED_STAINED_GLASS_PANE, BlockID.WHITE_STAINED_GLASS_PANE, BlockID.YELLOW_STAINED_GLASS_PANE, BlockID.IRON_BARS, BlockID.COBBLESTONE_WALL, BlockID.COBBLED_DEEPSLATE_WALL -> true
                else -> {
                    if (block is BlockTrapdoor) {
                        block.isOpen && block.blockFace == calculateFace(this.position, block.position)
                    }
                    block.isSolid
                }
            }
        }
        return false
    }
}
