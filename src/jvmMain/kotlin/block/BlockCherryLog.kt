package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCherryLog(blockState: BlockState = properties.defaultState) : BlockLog(blockState) {
    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 10.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 5

    override val name: String
        get() = "Cherry log"

    override fun getStrippedState(): BlockState {
        return BlockStrippedCherryLog.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHERRY_LOG, CommonBlockProperties.PILLAR_AXIS)
    }
}

