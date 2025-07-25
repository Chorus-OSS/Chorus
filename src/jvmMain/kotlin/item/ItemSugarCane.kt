package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockReeds


class ItemSugarCane @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.SUGAR_CANE, 0, count) {
    init {
        this.blockState = BlockReeds.properties.defaultState
    }
}
