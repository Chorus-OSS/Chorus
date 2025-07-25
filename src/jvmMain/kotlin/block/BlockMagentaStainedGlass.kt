package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockMagentaStainedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassStained(blockstate) {
    override fun getDyeColor() = DyeColor.MAGENTA

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MAGENTA_STAINED_GLASS)

    }
}