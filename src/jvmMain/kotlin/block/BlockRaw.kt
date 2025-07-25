package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

abstract class BlockRaw(blockstate: BlockState) : BlockSolid(blockstate) {
    override val hardness: Double
        get() = 5.0

    override val resistance: Double
        get() = 6.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override fun canHarvestWithHand(): Boolean {
        return false
    }
}
