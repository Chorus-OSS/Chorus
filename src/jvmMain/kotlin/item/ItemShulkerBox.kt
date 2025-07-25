package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemShulkerBox @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.SHULKER_BOX, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                this.name = "White Shulker Box"
                blockState = BlockWhiteShulkerBox.properties.defaultState
            }

            1 -> {
                this.name = "Orange Shulker Box"
                blockState = BlockOrangeShulkerBox.properties.defaultState
            }

            2 -> {
                this.name = "Magenta Shulker Box"
                blockState = BlockMagentaShulkerBox.properties.defaultState
            }

            3 -> {
                this.name = "Light Blue Shulker Box"
                blockState = BlockLightBlueShulkerBox.properties.defaultState
            }

            4 -> {
                this.name = "Yellow Shulker Box"
                blockState = BlockYellowShulkerBox.properties.defaultState
            }

            5 -> {
                this.name = "Lime Shulker Box"
                blockState = BlockLimeShulkerBox.properties.defaultState
            }

            6 -> {
                this.name = "Pink Shulker Box"
                blockState = BlockPinkShulkerBox.properties.defaultState
            }

            7 -> {
                this.name = "Gray Shulker Box"
                blockState = BlockGrayShulkerBox.properties.defaultState
            }

            8 -> {
                this.name = "Light Gray Shulker Box"
                blockState = BlockLightGrayShulkerBox.properties.defaultState
            }

            9 -> {
                this.name = "Cyan Shulker Box"
                blockState = BlockCyanShulkerBox.properties.defaultState
            }

            10 -> {
                this.name = "Purple Shulker Box"
                blockState = BlockPurpleShulkerBox.properties.defaultState
            }

            11 -> {
                this.name = "Blue Shulker Box"
                blockState = BlockBlueShulkerBox.properties.defaultState
            }

            12 -> {
                this.name = "Brown Shulker Box"
                blockState = BlockBrownShulkerBox.properties.defaultState
            }

            13 -> {
                this.name = "Green Shulker Box"
                blockState = BlockGreenShulkerBox.properties.defaultState
            }

            14 -> {
                this.name = "Red Shulker Box"
                blockState = BlockRedShulkerBox.properties.defaultState
            }

            15 -> {
                this.name = "Black Shulker Box"
                blockState = BlockBlackShulkerBox.properties.defaultState
            }
        }
        this.meta = 0
    }

    override val maxStackSize: Int = 1
}