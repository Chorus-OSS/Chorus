package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDarkOakSign

class BlockDarkoakStandingSign @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStandingSign(blockstate) {
    override fun getWallSignId(): String {
        return BlockDarkoakWallSign.properties.identifier
    }

    override fun toItem(): Item {
        return ItemDarkOakSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DARKOAK_STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}