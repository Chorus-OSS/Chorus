package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockRedStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {
    override fun getDyeColor() = DyeColor.RED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RED_STAINED_GLASS)
    }
}