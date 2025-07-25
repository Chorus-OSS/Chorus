package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockAcaciaLeaves
import org.chorus_oss.chorus.block.BlockDarkOakLeaves

class ItemLeaves2 @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.LEAVES2, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                name = "Acacia Leaves"
                blockState = BlockAcaciaLeaves.properties.defaultState
                this.meta = 0
            }

            1 -> {
                name = "Dark Oak Leaves"
                blockState = BlockDarkOakLeaves.properties.defaultState
                this.meta = 0
            }

            else -> throw IllegalArgumentException("Invalid damage: $damage")
        }
    }
}