package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemShulkerBox
import org.chorus_oss.chorus.tags.BlockTags

class BlockLightGrayShulkerBox @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockUndyedShulkerBox(blockstate) {
    override fun getShulkerBox(): Item {
        return ItemShulkerBox(8)
    }

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIGHT_GRAY_SHULKER_BOX, setOf(BlockTags.PNX_SHULKERBOX))

    }
}