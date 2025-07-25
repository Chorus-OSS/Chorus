package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityID

class BlockGlowFrame @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFrame(blockstate) {
    override val name: String
        get() = "Glow Item Frame"

    override fun getBlockEntityType(): String {
        return BlockEntityID.GLOW_ITEM_FRAME
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.GLOW_FRAME,
            CommonBlockProperties.FACING_DIRECTION,
            CommonBlockProperties.ITEM_FRAME_MAP_BIT,
            CommonBlockProperties.ITEM_FRAME_PHOTO_BIT
        )

    }
}