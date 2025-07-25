package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockStonecutter @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Stonecutter"

    override val hardness: Double
        get() = 3.5

    override val resistance: Double
        get() = 17.5

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.STONECUTTER)
    }
}
