package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemPlanks @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.PLANKS, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "Oak Planks"
                blockState = BlockOakPlanks.properties.defaultState
            }

            1 -> {
                this.name = "Spruce Planks"
                blockState = BlockSprucePlanks.properties.defaultState
            }

            2 -> {
                this.name = "Birch Planks"
                blockState = BlockBirchPlanks.properties.defaultState
            }

            3 -> {
                this.name = "Jungle Planks"
                blockState = BlockJunglePlanks.properties.defaultState
            }

            4 -> {
                this.name = "Acacia Planks"
                blockState = BlockAcaciaPlanks.properties.defaultState
            }

            5 -> {
                this.name = "Dark Oak Planks"
                blockState = BlockDarkOakPlanks.properties.defaultState
            }
        }
        this.meta = 0
    }
}