package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

abstract class BlockDoubleWoodenSlab(blockstate: BlockState) : BlockDoubleSlabBase(blockstate) {
    override val name: String
        get() = "Double ${getSlabName()} Wood Slab"

    override val resistance: Double
        get() = 15.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun isCorrectTool(item: Item): Boolean {
        return true
    }
}