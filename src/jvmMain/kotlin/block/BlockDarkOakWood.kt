package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockDarkOakWood(blockstate: BlockState) : BlockWood(blockstate) {
    override fun getWoodType(): WoodType {
        return WoodType.DARK_OAK
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DARK_OAK_WOOD, CommonBlockProperties.PILLAR_AXIS)
    }
}