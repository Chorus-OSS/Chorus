package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockPoppy

class ItemRedFlower : Item(ItemID.RED_FLOWER) {
    init {
        this.blockState = BlockPoppy.properties.defaultState
    }
}