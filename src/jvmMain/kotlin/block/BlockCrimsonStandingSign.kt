package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemCherrySign

class BlockCrimsonStandingSign @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStandingSign(blockstate) {
    override fun getWallSignId(): String {
        return BlockCrimsonWallSign.properties.identifier
    }

    override fun toItem(): Item {
        return ItemCherrySign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CRIMSON_STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}