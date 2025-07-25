package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockBirchLog
import org.chorus_oss.chorus.block.BlockJungleLog
import org.chorus_oss.chorus.block.BlockOakLog
import org.chorus_oss.chorus.block.BlockSpruceLog

class ItemLog @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.LOG, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "Oak Log"
                blockState = BlockOakLog.properties.defaultState
            }

            1 -> {
                this.name = "Spruce Log"
                blockState = BlockSpruceLog.properties.defaultState
            }

            2 -> {
                this.name = "Birch Log"
                blockState = BlockBirchLog.properties.defaultState
            }

            3 -> {
                this.name = "Jungle Log"
                blockState = BlockJungleLog.properties.defaultState
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