package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockUnpoweredComparator

class ItemComparator @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.COMPARATOR, meta, count, "Redstone Comparator") {
    init {
        this.blockState = BlockUnpoweredComparator.properties.defaultState
    }
}