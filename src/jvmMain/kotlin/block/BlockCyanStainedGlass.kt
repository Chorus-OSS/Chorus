package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockCyanStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {

    override fun getDyeColor(): DyeColor {
        return DyeColor.CYAN
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CYAN_STAINED_GLASS)
    }
}