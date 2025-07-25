package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSpruceSign

class BlockSpruceWallSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWallSign(blockState) {
    override val name: String
        get() = "Spruce Wall Sign"

    override fun getWallSignId(): String {
        return BlockID.SPRUCE_WALL_SIGN
    }

    override fun getStandingSignId(): String {
        return BlockID.SPRUCE_STANDING_SIGN
    }

    override fun toItem(): Item {
        return ItemSpruceSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SPRUCE_WALL_SIGN, CommonBlockProperties.FACING_DIRECTION)
    }
}
