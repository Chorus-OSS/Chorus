package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBrainCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFan(blockState) {
    override val name: String
        get() = "Brain Coral Fan"

    override fun getDeadCoralFan(): Block {
        return BlockDeadBrainCoralFan()
    }

    val wallFanId: String
        get() = BlockID.BRAIN_CORAL_WALL_FAN

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BRAIN_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}