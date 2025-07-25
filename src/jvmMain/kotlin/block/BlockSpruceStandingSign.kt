package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSpruceSign

class BlockSpruceStandingSign @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockStandingSign(blockstate) {
    override fun getWallSignId() = BlockSpruceWallSign.properties.identifier

    override fun toItem(): Item {
        return ItemSpruceSign()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SPRUCE_STANDING_SIGN, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}