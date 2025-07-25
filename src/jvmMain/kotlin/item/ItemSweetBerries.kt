package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockSweetBerryBush

class ItemSweetBerries @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.SWEET_BERRIES, meta, count, "Sweet Berries") {
    init {
        this.blockState = BlockSweetBerryBush.properties.defaultState
    }

    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 0.4f
}
