package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockSprucePressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockstate) {
    override val name: String
        get() = "Spruce Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SPRUCE_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}