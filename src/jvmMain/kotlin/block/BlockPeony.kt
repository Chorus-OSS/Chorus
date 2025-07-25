package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DoublePlantType

class BlockPeony(blockState: BlockState = properties.defaultState) : BlockDoublePlant(blockState) {
    override val doublePlantType: DoublePlantType
        get() = DoublePlantType.PAEONIA

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PEONY, CommonBlockProperties.UPPER_BLOCK_BIT)
    }
}