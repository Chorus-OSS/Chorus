package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

open class BlockEndStone(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "End Stone"

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 9.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.END_STONE)
    }
}
