package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockStrippedJungleLog @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodStripped(blockstate) {
    override fun getStrippedState(): BlockState {
        return BlockStrippedAcaciaLog.Companion.properties.defaultState
    }

    override fun getWoodType(): WoodType {
        return WoodType.JUNGLE
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_JUNGLE_LOG, CommonBlockProperties.PILLAR_AXIS)
    }
}