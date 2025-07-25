package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCrimsonStem @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStem(blockstate) {
    override fun getStrippedState(): BlockState {
        return BlockStrippedAcaciaLog.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRIMSON_STEM, CommonBlockProperties.PILLAR_AXIS)
    }
}