package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockMudBrickStairs(blockState: BlockState = properties.defaultState) : BlockStairs(blockState) {
    override val name: String
        get() = "Mud Bricks Stair"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.MUD_BRICK_STAIRS,
            CommonBlockProperties.UPSIDE_DOWN_BIT,
            CommonBlockProperties.WEIRDO_DIRECTION
        )
    }
}
