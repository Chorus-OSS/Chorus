package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemFence @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.FENCE, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "Oak Fence"
                blockState = BlockOakFence.properties.defaultState
                return
            }

            1 -> {
                this.name = "Spruce Fence"
                blockState = BlockSpruceFence.properties.defaultState
                this.meta = 0
                return
            }

            2 -> {
                this.name = "Birch Fence"
                blockState = BlockBirchFence.properties.defaultState
                this.meta = 0
                return
            }

            3 -> {
                this.name = "Jungle Fence"
                blockState = BlockJungleFence.properties.defaultState
                this.meta = 0
                return
            }

            4 -> {
                this.name = "Acacia Fence"
                blockState = BlockAcaciaFence.properties.defaultState
                this.meta = 0
                return
            }

            5 -> {
                this.name = "Dark Oak Fence"
                blockState = BlockDarkOakFence.properties.defaultState
                this.meta = 0
            }
        }
    }
}