package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockNormalStoneSlab(blockState: BlockState) : BlockSlab(blockState, BlockID.NORMAL_STONE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Stone"
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

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.NORMAL_STONE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}
