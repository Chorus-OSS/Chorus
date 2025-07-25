package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockResin @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockSolid(state) {
    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RESIN_BLOCK)
    }
}
