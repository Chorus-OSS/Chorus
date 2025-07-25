package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockRedstoneWire


class ItemRedstone @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.REDSTONE, meta, count, "Redstone Dust") {
    init {
        this.blockState = BlockRedstoneWire.properties.defaultState
    }
}
