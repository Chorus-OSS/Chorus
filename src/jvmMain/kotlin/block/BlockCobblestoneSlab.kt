package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockCobblestoneSlab(blockState: BlockState) : BlockSlab(blockState, BlockID.COBBLESTONE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Cobblestone"
    }

    override fun isSameType(slab: BlockSlab): Boolean {
        return this.id == slab.id
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
            BlockProperties(BlockID.COBBLESTONE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}