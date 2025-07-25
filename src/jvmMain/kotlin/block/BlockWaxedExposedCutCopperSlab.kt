package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWaxedExposedCutCopperSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockExposedCutCopperSlab(blockstate, BlockID.WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB) {
    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}