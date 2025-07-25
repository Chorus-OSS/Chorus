package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType

class BlockStrippedCherryLog @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodStripped(blockstate) {
    override val name: String
        get() = "Stripped Cherry Log"

    override fun getStrippedState(): BlockState {
        return BlockStrippedAcaciaLog.Companion.properties.defaultState
    }

    override fun getWoodType(): WoodType {
        throw UnsupportedOperationException()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_CHERRY_LOG, CommonBlockProperties.PILLAR_AXIS)
    }
}