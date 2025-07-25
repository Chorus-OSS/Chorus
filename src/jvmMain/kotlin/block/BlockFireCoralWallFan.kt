package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockFireCoralWallFan(blockState: BlockState = properties.defaultState) : BlockCoralWallFan(blockState) {
    override val name: String
        get() = "Fire Coral"

    override fun getDeadCoralFan() = BlockDeadFireCoralWallFan()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.FIRE_CORAL_WALL_FAN, CommonBlockProperties.CORAL_DIRECTION)
    }
}