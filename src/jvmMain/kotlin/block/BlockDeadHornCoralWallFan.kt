package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDeadHornCoralWallFan(blockState: BlockState = properties.defaultState) : BlockDeadCoralWallFan(blockState) {
    override val name: String
        get() = "Horn Coral"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DEAD_HORN_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}