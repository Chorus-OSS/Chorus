package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCrimsonStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStairsWood(blockstate) {
    override val name: String
        get() = "Crimson Wood Stairs"

    override val burnChance: Int
        get() = 0

    override val burnAbility: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CRIMSON_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}