package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockOrangeStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {
    override fun getDyeColor() = DyeColor.ORANGE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.ORANGE_STAINED_GLASS)

    }
}