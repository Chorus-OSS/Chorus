package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockMangrovePressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockstate) {
    init {
        this.onPitch = 0.8f
        this.offPitch = 0.7f
    }

    override val name: String
        get() = "Mangrove Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MANGROVE_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}