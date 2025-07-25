package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockCarrots


class ItemCarrot @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.CARROT, meta, count, "Carrot") {
    init {
        this.blockState = BlockCarrots.properties.defaultState
    }

    override val foodRestore: Int
        get() = 3

    override val saturationRestore: Float
        get() = 4.8f
}
