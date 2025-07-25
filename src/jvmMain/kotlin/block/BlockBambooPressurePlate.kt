package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockBambooPressurePlate(blockState: BlockState) : BlockWoodenPressurePlate(blockState) {
    override val name: String
        get() = "Bamboo Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BAMBOO_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}