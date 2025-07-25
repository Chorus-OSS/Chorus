package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemConcrete @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.CONCRETE, meta, count) {
    init {
        adjustName()
        adjustBlock()
    }

    private fun adjustBlock() {
        when (damage) {
            0 -> blockState = BlockWhiteConcrete.properties.defaultState
            1 -> blockState = BlockOrangeConcrete.properties.defaultState
            2 -> blockState = BlockMagentaConcrete.properties.defaultState
            3 -> blockState = BlockLightBlueConcrete.properties.defaultState
            4 -> blockState = BlockYellowConcrete.properties.defaultState
            5 -> blockState = BlockLimeConcrete.properties.defaultState
            6 -> blockState = BlockPinkConcrete.properties.defaultState
            7 -> blockState = BlockGrayConcrete.properties.defaultState
            8 -> blockState = BlockLightGrayConcrete.properties.defaultState
            9 -> blockState = BlockCyanConcrete.properties.defaultState
            10 -> blockState = BlockPurpleConcrete.properties.defaultState
            11 -> blockState = BlockBlueConcrete.properties.defaultState
            12 -> blockState = BlockBrownConcrete.properties.defaultState
            13 -> blockState = BlockGreenConcrete.properties.defaultState
            14 -> blockState = BlockRedConcrete.properties.defaultState
            15 -> blockState = BlockBlackConcrete.properties.defaultState
        }
    }

    private fun adjustName() {
        when (damage) {
            0 -> name = "White Concrete"
            1 -> name = "Orange Concrete"
            2 -> name = "Magenta Concrete"
            3 -> name = "Light Blue Concrete"
            4 -> name = "Yellow Concrete"
            5 -> name = "Lime Concrete"
            6 -> name = "Pink Concrete"
            7 -> name = "Gray Concrete"
            8 -> name = "Light Gray Concrete"
            9 -> name = "Cyan Concrete"
            10 -> name = "Purple Concrete"
            11 -> name = "Blue Concrete"
            12 -> name = "Brown Concrete"
            13 -> name = "Green Concrete"
            14 -> name = "Red Concrete"
            15 -> name = "Black Concrete"
        }
    }
}