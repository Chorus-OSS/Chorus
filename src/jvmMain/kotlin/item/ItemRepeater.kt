package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockUnpoweredRepeater

class ItemRepeater : Item(ItemID.REPEATER, 0, 1, "Redstone Repeater") {
    init {
        this.blockState = BlockUnpoweredRepeater.properties.defaultState
    }
}