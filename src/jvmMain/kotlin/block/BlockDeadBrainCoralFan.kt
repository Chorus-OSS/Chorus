package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDeadBrainCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFanDead(blockState) {
    val wallFanId: String
        get() = BlockID.DEAD_BRAIN_CORAL_WALL_FAN

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DEAD_BRAIN_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}