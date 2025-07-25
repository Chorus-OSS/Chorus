package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags

abstract class BlockHanging(blockState: BlockState) : BlockFlowable(blockState) {
    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL && !isSupportValid) {
            level.useBreakOn(this.position)
            return type
        }
        return 0
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        return isSupportValid && super.place(item, block, target, face, fx, fy, fz, player)
    }

    protected open val isSupportValid: Boolean
        get() = down().`is`(BlockTags.DIRT) || when (down().id) {
            BlockID.WARPED_NYLIUM, BlockID.CRIMSON_NYLIUM, BlockID.SOUL_SOIL -> true
            else -> false
        }

    override val burnChance: Int
        get() = 5
}
