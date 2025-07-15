package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockExposedCutCopperSlab(
    blockState: BlockState = properties.defaultState,
    doubleSlabID: String = BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB
) : BlockCutCopperSlab(blockState, doubleSlabID) {
    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.EXPOSED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.EXPOSED_CUT_COPPER_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}