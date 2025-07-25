package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockPurpurSlab(blockState: BlockState) : BlockSlab(blockState, BlockID.PURPUR_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Purpur"
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun isSameType(slab: BlockSlab): Boolean {
        return slab.id == this.id
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PURPUR_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
