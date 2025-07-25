package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockGrayStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {

    override fun getDyeColor() = DyeColor.GRAY

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GRAY_STAINED_GLASS_PANE)
    }
}