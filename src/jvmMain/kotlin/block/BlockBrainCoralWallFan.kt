package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBrainCoralWallFan(blockState: BlockState = properties.defaultState) : BlockCoralWallFan(blockState) {
    override val name: String
        get() = "Brain Coral"

    override fun getDeadCoralFan(): Block {
        return BlockDeadBrainCoralWallFan()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BRAIN_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}