package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType

class BlockSunflower(blockState: BlockState = properties.defaultState) : BlockDoublePlant(blockState) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.SUNFLOWER

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SUNFLOWER, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}