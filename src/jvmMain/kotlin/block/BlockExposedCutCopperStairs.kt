package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel

open class BlockExposedCutCopperStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCutCopperStairs(blockstate) {

    override val oxidizationLevel: OxidizationLevel
        get() = OxidizationLevel.EXPOSED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.EXPOSED_CUT_COPPER_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}