package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockBlueStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {
    override fun getDyeColor(): DyeColor {
        return DyeColor.BLUE
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BLUE_STAINED_GLASS_PANE)
    }
}