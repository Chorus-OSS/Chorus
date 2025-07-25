package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWaxedDoubleCutCopperSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleCutCopperSlab(blockstate) {
    override fun getSingleSlab() = BlockWaxedCutCopperSlab.properties.defaultState

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WAXED_DOUBLE_CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)

    }
}