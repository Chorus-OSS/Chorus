package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

//todo complete
class BlockPitcherCrop @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCrops(blockstate) {
    override val name: String
        get() = "Pitcher Crop"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PITCHER_CROP, CommonBlockProperties.GROWTH, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}