package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockMangroveLog(blockState: BlockState = properties.defaultState) : BlockLog(blockState) {
    override val name: String
        get() = "Mangrove Log"

    override fun getStrippedState(): BlockState {
        return BlockStrippedMangroveLog.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MANGROVE_LOG, CommonBlockProperties.PILLAR_AXIS)
    }
}
