package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWaxedExposedCopperTrapdoor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperTrapdoor(blockstate) {
    override val name: String
        get() = "Waxed Exposed Copper Trapdoor"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WAXED_EXPOSED_COPPER_TRAPDOOR,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPSIDE_DOWN_BIT
        )

    }
}