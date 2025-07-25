package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockTripWire


class ItemString @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.STRING, meta, count, "String") {
    init {
        this.blockState = BlockTripWire.properties.defaultState
    }
}
