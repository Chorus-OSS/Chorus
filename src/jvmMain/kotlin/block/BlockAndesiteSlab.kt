package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockAndesiteSlab(blockState: BlockState) : BlockSlab(blockState, BlockID.ANDESITE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Andesite"
    }

    override fun isSameType(slab: BlockSlab): Boolean {
        return slab.id == this.id
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val hardness: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.ANDESITE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
