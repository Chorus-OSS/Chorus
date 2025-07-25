package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockAcaciaStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStairsWood(blockstate) {
    override val name: String
        get() = "Acacia Wood Stairs"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.ACACIA_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}