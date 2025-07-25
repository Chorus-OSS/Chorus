package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType

class BlockLilac(blockState: BlockState = properties.defaultState) : BlockDoublePlant(blockState) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.SYRINGA

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LILAC, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}