package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCherryStairs @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockStairsWood(blockState) {
    override val name: String
        get() = "Cherry Wood Stairs"

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 3.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CHERRY_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}