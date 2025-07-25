package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.tags.BlockTags

class BlockLightBlueShulkerBox @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockUndyedShulkerBox(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIGHT_BLUE_SHULKER_BOX, setOf(BlockTags.PNX_SHULKERBOX))
    }
}