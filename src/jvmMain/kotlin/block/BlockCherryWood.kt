package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockCherryWood @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWood(blockState) {
    override val name: String
        get() = "Cherry Wood"

    override fun getWoodType(): WoodType {
        return WoodType.CHERRY
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHERRY_WOOD, CommonBlockProperties.PILLAR_AXIS)
    }
}