package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockPaleOakSapling(blockState: BlockState = properties.defaultState) : BlockSapling(blockState) {
    override fun getWoodType(): WoodType {
        return WoodType.PALE_OAK
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PALE_OAK_SAPLING, CommonBlockProperties.AGE_BIT)
    }
}