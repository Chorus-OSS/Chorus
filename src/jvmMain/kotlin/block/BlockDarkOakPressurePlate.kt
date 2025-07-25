package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDarkOakPressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockstate) {
    override val name: String
        get() = "Dark Oak Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DARK_OAK_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}