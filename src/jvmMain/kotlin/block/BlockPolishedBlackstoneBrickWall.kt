package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPolishedBlackstoneBrickWall @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPolishedBlackstoneWall(blockstate) {
    override val name: String
        get() = "Polished Blackstone Brick Wall"

    override val hardness: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.POLISHED_BLACKSTONE_BRICK_WALL,
            CommonBlockProperties.WALL_CONNECTION_TYPE_EAST,
            CommonBlockProperties.WALL_CONNECTION_TYPE_NORTH,
            CommonBlockProperties.WALL_CONNECTION_TYPE_SOUTH,
            CommonBlockProperties.WALL_CONNECTION_TYPE_WEST,
            CommonBlockProperties.WALL_POST_BIT
        )
    }
}