package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPaleOakSign

class BlockPaleOakWallSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWallSign(blockState) {
    override fun getWallSignId(): String {
        return BlockID.PALE_OAK_WALL_SIGN
    }

    override fun getStandingSignId(): String {
        return BlockID.PALE_OAK_STANDING_SIGN
    }

    override val name: String
        get() = "Pale Oak Wall Sign"

    override fun toItem(): Item {
        return ItemPaleOakSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PALE_OAK_WALL_SIGN, CommonBlockProperties.FACING_DIRECTION)

    }
}
