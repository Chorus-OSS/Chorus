package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockPinkStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {
    override fun getDyeColor() = DyeColor.PINK

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PINK_STAINED_GLASS)

    }
}