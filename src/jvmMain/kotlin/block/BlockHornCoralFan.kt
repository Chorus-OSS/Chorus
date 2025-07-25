package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockHornCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFan(blockState) {
    override val name: String
        get() = "Horn Coral Fan"

    override fun getDeadCoralFan() = BlockDeadHornCoralFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.HORN_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}