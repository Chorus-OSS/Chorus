package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockSpruceSapling(blockState: BlockState = properties.defaultState) : BlockSapling(blockState) {
    override fun getWoodType() = WoodType.SPRUCE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SPRUCE_SAPLING, CommonBlockProperties.AGE_BIT)
    }
}