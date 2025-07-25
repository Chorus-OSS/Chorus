package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

abstract class BlockMushroomBlock(blockState: BlockState) : BlockSolid(blockState) {
    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val hardness: Double
        get() = 0.2

    override val resistance: Double
        get() = 0.2

    override fun canSilkTouch(): Boolean {
        return true
    }
}
