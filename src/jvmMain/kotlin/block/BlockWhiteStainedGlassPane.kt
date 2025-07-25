package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockWhiteStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {
    override fun getDyeColor() = DyeColor.WHITE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WHITE_STAINED_GLASS_PANE)
    }
}