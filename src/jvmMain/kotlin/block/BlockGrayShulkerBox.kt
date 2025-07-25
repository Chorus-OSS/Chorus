package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemShulkerBox
import org.chorus_oss.chorus.tags.BlockTags

class BlockGrayShulkerBox @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockUndyedShulkerBox(blockstate) {
    override fun getShulkerBox(): Item {
        return ItemShulkerBox(7)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GRAY_SHULKER_BOX, setOf(BlockTags.PNX_SHULKERBOX))
    }
}