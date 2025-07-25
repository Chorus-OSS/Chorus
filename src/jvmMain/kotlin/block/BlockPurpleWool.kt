package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.tags.BlockTags
import org.chorus_oss.chorus.utils.DyeColor

class BlockPurpleWool @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWool(blockstate) {
    override fun getDyeColor(): DyeColor {
        return DyeColor.PURPLE
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PURPLE_WOOL, setOf(BlockTags.PNX_WOOL))
    }
}