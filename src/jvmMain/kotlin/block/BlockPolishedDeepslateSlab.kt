package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockPolishedDeepslateSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSlab(blockstate, BlockID.POLISHED_DEEPSLATE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Polished Deepslate"
    }

    override val hardness: Double
        get() = 3.5

    override val resistance: Double
        get() = 6.0

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun isSameType(slab: BlockSlab): Boolean {
        return id == slab.id
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.POLISHED_DEEPSLATE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}