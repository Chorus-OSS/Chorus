package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

abstract class BlockGlassPaneStained(blockState: BlockState) : BlockGlassPane(blockState) {
    abstract fun getDyeColor(): DyeColor
}
