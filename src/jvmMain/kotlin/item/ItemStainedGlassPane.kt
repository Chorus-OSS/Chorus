package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemStainedGlassPane @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.STAINED_GLASS_PANE, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "White Stained Glass Pane"
                blockState = BlockWhiteStainedGlassPane.properties.defaultState
            }

            1 -> {
                this.name = "Orange Stained Glass Pane"
                blockState = BlockOrangeStainedGlassPane.properties.defaultState
            }

            2 -> {
                this.name = "Magenta Stained Glass Pane"
                blockState = BlockMagentaStainedGlassPane.properties.defaultState
            }

            3 -> {
                this.name = "Light Blue Stained Glass Pane"
                blockState = BlockLightBlueStainedGlassPane.properties.defaultState
            }

            4 -> {
                this.name = "Yellow Stained Glass Pane"
                blockState = BlockYellowStainedGlassPane.properties.defaultState
            }

            5 -> {
                this.name = "Lime Stained Glass Pane"
                blockState = BlockLimeStainedGlassPane.properties.defaultState
            }

            6 -> {
                this.name = "Pink Stained Glass Pane"
                blockState = BlockPinkStainedGlassPane.properties.defaultState
            }

            7 -> {
                this.name = "Gray Stained Glass Pane"
                blockState = BlockGrayStainedGlassPane.properties.defaultState
            }

            8 -> {
                this.name = "Light Gray Stained Glass Pane"
                blockState = BlockLightGrayStainedGlassPane.properties.defaultState
            }

            9 -> {
                this.name = "Cyan Stained Glass Pane"
                blockState = BlockCyanStainedGlassPane.properties.defaultState
            }

            10 -> {
                this.name = "Purple Stained Glass Pane"
                blockState = BlockPurpleStainedGlassPane.properties.defaultState
            }

            11 -> {
                this.name = "Blue Stained Glass Pane"
                blockState = BlockBlueStainedGlassPane.properties.defaultState
            }

            12 -> {
                this.name = "Brown Stained Glass Pane"
                blockState = BlockBrownStainedGlassPane.properties.defaultState
            }

            13 -> {
                this.name = "Green Stained Glass Pane"
                blockState = BlockGreenStainedGlassPane.properties.defaultState
            }

            14 -> {
                this.name = "Red Stained Glass Pane"
                blockState = BlockRedStainedGlassPane.properties.defaultState
            }

            15 -> {
                this.name = "Black Stained Glass Pane"
                blockState = BlockBlackStainedGlassPane.properties.defaultState
            }
        }
        this.meta = 0
    }
}