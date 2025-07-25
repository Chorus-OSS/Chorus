package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockHornCoralWallFan(blockState: BlockState = properties.defaultState) : BlockCoralWallFan(blockState) {
    override val name: String
        get() = "Horn Coral"

    override fun getDeadCoralFan() = BlockDeadHornCoralWallFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.HORN_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}