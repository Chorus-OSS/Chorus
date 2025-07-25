package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemConcretePowder @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.CONCRETE_POWDER, meta, count) {
    init {
        adjustName()
        adjustBlock()
    }

    private fun adjustBlock() {
        when (damage) {
            0 -> blockState = BlockWhiteConcretePowder.properties.defaultState
            1 -> blockState = BlockOrangeConcretePowder.properties.defaultState
            2 -> blockState = BlockMagentaConcretePowder.properties.defaultState
            3 -> blockState = BlockLightBlueConcretePowder.properties.defaultState
            4 -> blockState = BlockYellowConcretePowder.properties.defaultState
            5 -> blockState = BlockLimeConcretePowder.properties.defaultState
            6 -> blockState = BlockPinkConcretePowder.properties.defaultState
            7 -> blockState = BlockGrayConcretePowder.properties.defaultState
            8 -> blockState = BlockLightGrayConcretePowder.properties.defaultState
            9 -> blockState = BlockCyanConcretePowder.properties.defaultState
            10 -> blockState = BlockPurpleConcretePowder.properties.defaultState
            11 -> blockState = BlockBlueConcretePowder.properties.defaultState
            12 -> blockState = BlockBrownConcretePowder.properties.defaultState
            13 -> blockState = BlockGreenConcretePowder.properties.defaultState
            14 -> blockState = BlockRedConcretePowder.properties.defaultState
            15 -> blockState = BlockBlackConcretePowder.properties.defaultState
        }
    }

    private fun adjustName() {
        when (damage) {
            0 -> name = "White Concrete Powder"
            1 -> name = "Orange Concrete Powder"
            2 -> name = "Magenta Concrete Powder"
            3 -> name = "Light Blue Concrete Powder"
            4 -> name = "Yellow Concrete Powder"
            5 -> name = "Lime Concrete Powder"
            6 -> name = "Pink Concrete Powder"
            7 -> name = "Gray Concrete Powder"
            8 -> name = "Light Gray Concrete Powder"
            9 -> name = "Cyan Concrete Powder"
            10 -> name = "Purple Concrete Powder"
            11 -> name = "Blue Concrete Powder"
            12 -> name = "Brown Concrete Powder"
            13 -> name = "Green Concrete Powder"
            14 -> name = "Red Concrete Powder"
            15 -> name = "Black Concrete Powder"
        }
    }
}