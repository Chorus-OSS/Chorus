package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDeadFireCoralWallFan(blockState: BlockState = properties.defaultState) : BlockDeadCoralWallFan(blockState) {
    override val name: String
        get() = "Fire Coral"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DEAD_FIRE_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}