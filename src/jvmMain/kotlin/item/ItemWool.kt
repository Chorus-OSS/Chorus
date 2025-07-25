package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemWool @JvmOverloads constructor(aux: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.WOOL, aux, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "White Wool"
                blockState = BlockWhiteWool.properties.defaultState
            }

            1 -> {
                this.name = "Orange Wool"
                blockState = BlockOrangeWool.properties.defaultState
            }

            2 -> {
                this.name = "Magenta Wool"
                blockState = BlockMagentaWool.properties.defaultState
            }

            3 -> {
                this.name = "Light Blue Wool"
                blockState = BlockLightBlueWool.properties.defaultState
            }

            4 -> {
                this.name = "Yellow Wool"
                blockState = BlockYellowWool.properties.defaultState
            }

            5 -> {
                this.name = "Lime Wool"
                blockState = BlockLimeWool.properties.defaultState
            }

            6 -> {
                this.name = "Pink Wool"
                blockState = BlockPinkWool.properties.defaultState
            }

            7 -> {
                this.name = "Gray Wool"
                blockState = BlockGrayWool.properties.defaultState
            }

            8 -> {
                this.name = "Light Gray Wool"
                blockState = BlockLightGrayWool.properties.defaultState
            }

            9 -> {
                this.name = "Cyan Wool"
                blockState = BlockCyanWool.properties.defaultState
            }

            10 -> {
                this.name = "Purple Wool"
                blockState = BlockPurpleWool.properties.defaultState
            }

            11 -> {
                this.name = "Blue Wool"
                blockState = BlockBlueWool.properties.defaultState
            }

            12 -> {
                this.name = "Brown Wool"
                blockState = BlockBrownWool.properties.defaultState
            }

            13 -> {
                this.name = "Green Wool"
                blockState = BlockGreenWool.properties.defaultState
            }

            14 -> {
                this.name = "Red Wool"
                blockState = BlockRedWool.properties.defaultState
            }

            15 -> {
                this.name = "Black Wool"
                blockState = BlockBlackWool.properties.defaultState
            }
        }
        this.meta = 0
    }
}