package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockID


class ItemBeetroot @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(BlockID.BEETROOT, meta, count, "Beetroot") {
    override val foodRestore: Int
        get() = 1

    override val saturationRestore: Float
        get() = 1.2f
}
