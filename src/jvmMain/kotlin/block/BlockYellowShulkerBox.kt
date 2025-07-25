package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemShulkerBox
import org.chorus_oss.chorus.tags.BlockTags

class BlockYellowShulkerBox @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockUndyedShulkerBox(blockstate) {
    override fun getShulkerBox() = ItemShulkerBox(4)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.YELLOW_SHULKER_BOX, setOf(BlockTags.PNX_SHULKERBOX))
    }
}