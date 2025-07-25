package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockTubeCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFan(blockState) {
    override val name: String
        get() = "Tube Coral Fan"

    override fun getDeadCoralFan() = BlockDeadTubeCoralFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.TUBE_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}