package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPolishedBlackstoneBrickStairs @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPolishedBlackstoneStairs(blockstate) {
    override val name: String
        get() = "Polished Blackstone Brick Stairs"

    override val hardness: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.POLISHED_BLACKSTONE_BRICK_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )

    }
}