package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockState

abstract class ItemHangingSign(blockState: BlockState) : Item(blockState.identifier) {
    init {
        this.blockState = blockState
    }

    override val maxStackSize: Int
        get() = 16
}
