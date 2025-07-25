package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemStainedHardenedClay @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.STAINED_HARDENED_CLAY, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "White Terracotta"
                blockState = BlockWhiteTerracotta.properties.defaultState
            }

            1 -> {
                this.name = "Orange Terracotta"
                blockState = BlockOrangeTerracotta.properties.defaultState
            }

            2 -> {
                this.name = "Magenta Terracotta"
                blockState = BlockMagentaTerracotta.properties.defaultState
            }

            3 -> {
                this.name = "Light Blue Terracotta"
                blockState = BlockLightBlueTerracotta.properties.defaultState
            }

            4 -> {
                this.name = "Yellow Terracotta"
                blockState = BlockYellowTerracotta.properties.defaultState
            }

            5 -> {
                this.name = "Lime Terracotta"
                blockState = BlockLimeTerracotta.properties.defaultState
            }

            6 -> {
                this.name = "Pink Terracotta"
                blockState = BlockPinkTerracotta.properties.defaultState
            }

            7 -> {
                this.name = "Gray Terracotta"
                blockState = BlockGrayTerracotta.properties.defaultState
            }

            8 -> {
                this.name = "Light Gray Terracotta"
                blockState = BlockLightGrayTerracotta.properties.defaultState
            }

            9 -> {
                this.name = "Cyan Terracotta"
                blockState = BlockCyanTerracotta.properties.defaultState
            }

            10 -> {
                this.name = "Purple Terracotta"
                blockState = BlockPurpleTerracotta.properties.defaultState
            }

            11 -> {
                this.name = "Blue Terracotta"
                blockState = BlockBlueTerracotta.properties.defaultState
            }

            12 -> {
                this.name = "Brown Terracotta"
                blockState = BlockBrownTerracotta.properties.defaultState
            }

            13 -> {
                this.name = "Green Terracotta"
                blockState = BlockGreenTerracotta.properties.defaultState
            }

            14 -> {
                this.name = "Red Terracotta"
                blockState = BlockRedTerracotta.properties.defaultState
            }

            15 -> {
                this.name = "Black Terracotta"
                blockState = BlockBlackTerracotta.properties.defaultState
            }
        }
        this.meta = 0
    }
}