package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockAcaciaLog
import org.chorus_oss.chorus.block.BlockDarkOakLog

class ItemLog2 @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.LOG2, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "Acacia Log"
                this.blockState = BlockAcaciaLog.properties.defaultState
                this.name = "Dark Oak Log"
                this.blockState = BlockDarkOakLog.properties.defaultState
            }

            1 -> {
                this.name = "Dark Oak Log"
                this.blockState = BlockDarkOakLog.properties.defaultState
            }
        }
        this.meta = 0
    }


    override fun equalItemBlock(item: Item): Boolean {
        if (this.isBlock() && item.isBlock()) {
            return this.getSafeBlockState() == item.getSafeBlockState()
        }
        return true
    }
}