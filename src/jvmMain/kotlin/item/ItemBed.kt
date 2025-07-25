package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockBed
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.utils.DyeColor


class ItemBed @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(BlockID.BED, meta, count) {
    override fun internalAdjust() {
        name = DyeColor.getByWoolData(meta).name + " Bed"
        blockState = BlockBed.properties.defaultState
    }

    override val maxStackSize: Int
        get() = 1
}
