package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPaleOakSign

class BlockPaleOakStandingSign @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStandingSign(blockstate) {
    override fun getStandingSignId(): String {
        return Companion.properties.identifier
    }

    override fun getWallSignId(): String {
        return BlockPaleOakWallSign.properties.identifier
    }

    override fun toItem(): Item {
        return ItemPaleOakSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PALE_OAK_STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}