package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

class BlockBlackStainedGlassPane @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlassPaneStained(blockstate) {
    override fun getDyeColor(): DyeColor {
        return DyeColor.BLACK
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BLACK_STAINED_GLASS_PANE)
    }
}