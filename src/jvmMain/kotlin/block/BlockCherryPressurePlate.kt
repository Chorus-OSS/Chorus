package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCherryPressurePlate @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockState) {
    override val name: String
        get() = "Cherry Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHERRY_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}