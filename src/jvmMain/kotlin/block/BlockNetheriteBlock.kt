package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockNetheriteBlock(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Netherite Block"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val hardness: Double
        get() = 50.0

    override val resistance: Double
        get() = 1200.0

    override val toolTier: Int
        get() = ItemTool.TIER_DIAMOND

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHERITE_BLOCK)
    }
}
