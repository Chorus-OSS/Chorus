package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

open class BlockBlackstone @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockSolid(blockState) {
    override val name: String
        get() = "Blackstone"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 6.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BLACKSTONE)
    }
}
