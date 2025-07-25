package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockExposedDoubleCutCopperSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleCutCopperSlab(blockstate) {
    override fun getSingleSlab() = BlockExposedCutCopperSlab.properties.defaultState

    override val oxidizationLevel = OxidizationLevel.EXPOSED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}