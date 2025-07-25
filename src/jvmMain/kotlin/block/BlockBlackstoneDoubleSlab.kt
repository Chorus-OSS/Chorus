package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockBlackstoneDoubleSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleSlabBase(blockstate) {
    override fun getSlabName(): String {
        return "Blackstone"
    }

    override val resistance: Double
        get() = 6.0

    override val hardness: Double
        get() = 2.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun getSingleSlab(): BlockState {
        return BlockBlackstoneSlab.properties.defaultState
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BLACKSTONE_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}