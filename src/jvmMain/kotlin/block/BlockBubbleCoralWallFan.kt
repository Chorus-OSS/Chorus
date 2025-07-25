package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBubbleCoralWallFan(blockState: BlockState = properties.defaultState) : BlockCoralWallFan(blockState) {
    override val name: String
        get() = "Bubble Coral"

    override fun getDeadCoralFan(): Block {
        return BlockDeadBubbleCoralWallFan()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BUBBLE_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}