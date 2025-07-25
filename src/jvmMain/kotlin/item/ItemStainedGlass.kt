package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemStainedGlass @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.STAINED_GLASS, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "White Stained Glass"
                blockState = BlockWhiteStainedGlass.properties.defaultState
            }

            1 -> {
                this.name = "Orange Stained Glass"
                blockState = BlockOrangeStainedGlass.properties.defaultState
            }

            2 -> {
                this.name = "Magenta Stained Glass"
                blockState = BlockMagentaStainedGlass.properties.defaultState
            }

            3 -> {
                this.name = "Light Blue Stained Glass"
                blockState = BlockLightBlueStainedGlass.properties.defaultState
            }

            4 -> {
                this.name = "Yellow Stained Glass"
                blockState = BlockYellowStainedGlass.properties.defaultState
            }

            5 -> {
                this.name = "Lime Stained Glass"
                blockState = BlockLimeStainedGlass.properties.defaultState
            }

            6 -> {
                this.name = "Pink Stained Glass"
                blockState = BlockPinkStainedGlass.properties.defaultState
            }

            7 -> {
                this.name = "Gray Stained Glass"
                blockState = BlockGrayStainedGlass.properties.defaultState
            }

            8 -> {
                this.name = "Light Gray Stained Glass"
                blockState = BlockLightGrayStainedGlass.properties.defaultState
            }

            9 -> {
                this.name = "Cyan Stained Glass"
                blockState = BlockCyanStainedGlass.properties.defaultState
            }

            10 -> {
                this.name = "Purple Stained Glass"
                blockState = BlockPurpleStainedGlass.properties.defaultState
            }

            11 -> {
                this.name = "Blue Stained Glass"
                blockState = BlockBlueStainedGlass.properties.defaultState
            }

            12 -> {
                this.name = "Brown Stained Glass"
                blockState = BlockBrownStainedGlass.properties.defaultState
            }

            13 -> {
                this.name = "Green Stained Glass"
                blockState = BlockGreenStainedGlass.properties.defaultState
            }

            14 -> {
                this.name = "Red Stained Glass"
                blockState = BlockRedStainedGlass.properties.defaultState
            }

            15 -> {
                this.name = "Black Stained Glass"
                blockState = BlockBlackStainedGlass.properties.defaultState
            }
        }
        this.meta = 0
    }
}