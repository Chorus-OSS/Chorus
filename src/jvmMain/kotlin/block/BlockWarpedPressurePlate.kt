package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockWarpedPressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenPressurePlate(blockstate) {
    override val name: String
        get() = "Warped Pressure Plate"

    override val burnChance: Int
        get() = 0

    override val burnAbility: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WARPED_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}