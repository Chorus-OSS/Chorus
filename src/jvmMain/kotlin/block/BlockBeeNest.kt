package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBeeNest @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBeehive(blockstate) {
    override val name: String
        get() = "Bee Nest"

    override val burnChance: Int
        get() = 30

    override val burnAbility: Int
        get() = 60

    override val hardness: Double
        get() = 0.3

    override val resistance: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BEE_NEST, CommonBlockProperties.DIRECTION, CommonBlockProperties.HONEY_LEVEL)
    }
}
