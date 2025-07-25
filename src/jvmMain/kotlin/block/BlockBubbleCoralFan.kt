package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBubbleCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFan(blockState) {
    override val name: String
        get() = "Bubble Coral Fan"

    override fun getDeadCoralFan(): Block {
        return BlockDeadBubbleCoralFan()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BUBBLE_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}