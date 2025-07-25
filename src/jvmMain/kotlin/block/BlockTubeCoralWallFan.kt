package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockTubeCoralWallFan(blockState: BlockState = properties.defaultState) : BlockCoralWallFan(blockState) {
    override val name: String
        get() = "Tube Coral"

    override fun getDeadCoralFan() = BlockDeadTubeCoralWallFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.TUBE_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}