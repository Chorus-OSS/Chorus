package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWaxedWeatheredCutCopperStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWeatheredCutCopperStairs(blockstate) {
    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WAXED_WEATHERED_CUT_COPPER_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}