package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDarkOakSign

class BlockDarkoakWallSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWallSign(blockState) {
    override fun getWallSignId(): String {
        return BlockID.DARKOAK_WALL_SIGN
    }

    override fun getStandingSignId(): String {
        return BlockID.DARKOAK_STANDING_SIGN
    }

    override fun toItem(): Item {
        return ItemDarkOakSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DARKOAK_WALL_SIGN, CommonBlockProperties.FACING_DIRECTION)
    }
}
