package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPaleOakPressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockstate) {
    override val name: String
        get() = "Pale Oak Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PALE_OAK_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}