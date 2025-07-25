package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockLightGrayStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {
    override fun getDyeColor() = DyeColor.LIGHT_GRAY

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_GRAY_STAINED_GLASS_PANE)

    }
}