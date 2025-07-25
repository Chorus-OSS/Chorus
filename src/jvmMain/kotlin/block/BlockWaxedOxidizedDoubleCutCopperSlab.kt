package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWaxedOxidizedDoubleCutCopperSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOxidizedDoubleCutCopperSlab(blockstate) {
    override fun getSingleSlab() = BlockWaxedOxidizedCutCopperSlab.Companion.properties.defaultState

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB,
            CommonBlockProperties.MINECRAFT_VERTICAL_HALF
        )
    }
}