package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockMossyCobblestoneSlab(blockState: BlockState) :
    BlockSlab(blockState, BlockID.MOSSY_COBBLESTONE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Mossy Cobblestone"
    }

    override fun isSameType(slab: BlockSlab): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MOSSY_COBBLESTONE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
