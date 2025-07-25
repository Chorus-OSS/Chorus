package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockLightBlueStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {
    override fun getDyeColor() = DyeColor.LIGHT_BLUE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_BLUE_STAINED_GLASS_PANE)
    }
}