package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockAcaciaSapling(blockState: BlockState = properties.defaultState) : BlockSapling(blockState) {
    override fun getWoodType(): WoodType {
        return WoodType.ACACIA
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.ACACIA_SAPLING, CommonBlockProperties.AGE_BIT)

    }
}