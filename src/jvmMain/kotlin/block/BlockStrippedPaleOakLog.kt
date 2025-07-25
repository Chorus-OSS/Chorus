package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockStrippedPaleOakLog @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodStripped(blockstate) {
    override fun getWoodType(): WoodType {
        return WoodType.PALE_OAK
    }

    override fun getStrippedState(): BlockState {
        return Companion.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_PALE_OAK_LOG, CommonBlockProperties.PILLAR_AXIS)
    }
}