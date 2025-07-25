package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockMelonStem

class ItemMelonSeeds @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.MELON_SEEDS, meta, count, "Melon Seeds") {
    init {
        this.blockState = BlockMelonStem.properties.defaultState
    }
}