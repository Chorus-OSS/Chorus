package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemCherrySign

class BlockCherryStandingSign @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockStandingSign(blockState) {
    override fun getWallSignId(): String {
        return BlockCherryWallSign.properties.identifier
    }

    override fun toItem(): Item {
        return ItemCherrySign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHERRY_STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}