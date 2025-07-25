package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPolishedBlackstonePressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStonePressurePlate(blockstate) {
    override val name: String
        get() = "Polished Blackstone Pressure Plate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.POLISHED_BLACKSTONE_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}