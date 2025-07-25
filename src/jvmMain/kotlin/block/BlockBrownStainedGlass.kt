package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockBrownStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {
    override fun getDyeColor(): DyeColor {
        return DyeColor.BROWN
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BROWN_STAINED_GLASS)
    }
}