package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType

class BlockLargeFern(blockState: BlockState = properties.defaultState) : BlockDoublePlant(blockState) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.FERN

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LARGE_FERN, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}