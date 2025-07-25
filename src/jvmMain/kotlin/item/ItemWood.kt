package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemWood @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.WOOD, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0, 6, 7, 14, 15 -> {
                name = "Oak Wood"
                blockState = BlockOakWood.properties.defaultState
            }

            1 -> {
                name = "Spruce Wood"
                blockState = BlockSpruceWood.properties.defaultState
            }

            2 -> {
                name = "Birch Wood"
                blockState = BlockBirchWood.properties.defaultState
            }

            3 -> {
                name = "Jungle Wood"
                blockState = BlockJungleWood.properties.defaultState
            }

            4 -> {
                name = "Acacia Wood"
                blockState = BlockAcaciaWood.properties.defaultState
            }

            5 -> {
                name = "Dark Oak Wood"
                blockState = BlockDarkOakWood.properties.defaultState
            }

            8 -> {
                name = "Stripped Oak Wood"
                blockState = BlockStrippedOakWood.properties.defaultState
            }

            9 -> {
                name = "Stripped Spruce Wood"
                blockState = BlockStrippedSpruceWood.properties.defaultState
            }

            10 -> {
                name = "Stripped Birch Wood"
                blockState = BlockStrippedBirchWood.properties.defaultState
            }

            11 -> {
                name = "Stripped Jungle Wood"
                blockState = BlockStrippedJungleWood.properties.defaultState
            }

            12 -> {
                name = "Stripped Acacia Wood"
                blockState = BlockStrippedAcaciaWood.properties.defaultState
            }

            13 -> {
                name = "Stripped Dark Oak Wood"
                blockState = BlockStrippedDarkOakWood.properties.defaultState
            }

            else -> throw IllegalArgumentException("Invalid damage: $damage")
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