package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.utils.DyeColor

abstract class BlockGlassStained(blockState: BlockState) : BlockGlass(blockState) {
    abstract fun getDyeColor(): DyeColor
}
