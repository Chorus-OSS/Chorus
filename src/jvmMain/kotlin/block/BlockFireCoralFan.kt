package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockFireCoralFan(blockState: BlockState = properties.defaultState) : BlockCoralFan(blockState) {
    override val name: String
        get() = "Fire Coral Fan"

    override fun getDeadCoralFan() = BlockDeadFireCoralFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.FIRE_CORAL_FAN, CommonBlockProperties.CORAL_FAN_DIRECTION)
    }
}