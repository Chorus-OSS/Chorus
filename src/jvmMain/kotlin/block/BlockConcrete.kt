package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

abstract class BlockConcrete(blockState: BlockState) : BlockSolid(blockState) {
    override val resistance: Double
        get() = 9.0

    override val hardness: Double
        get() = 1.8

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN
}
