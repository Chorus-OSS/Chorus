package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockCrimsonSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSlab(blockstate, BlockID.CRIMSON_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Crimson"
    }

    override fun isSameType(slab: BlockSlab): Boolean {
        return id == slab.id
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val hardness: Double
        get() = 3.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CRIMSON_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}