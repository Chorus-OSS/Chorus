package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*

class ItemCoral : Item {
    constructor() : super(ItemID.Companion.CORAL)

    @JvmOverloads
    constructor(meta: Int, count: Int = 1) : super(ItemID.Companion.CONCRETE, meta, count) {
        adjustName()
        adjustBlock()
    }

    private fun adjustBlock() {
        when (damage) {
            0 -> this.blockState = BlockTubeCoral.properties.defaultState
            1 -> this.blockState = BlockBrainCoral.properties.defaultState
            2 -> this.blockState = BlockBubbleCoral.properties.defaultState
            3 -> this.blockState = BlockFireCoral.properties.defaultState
            4 -> this.blockState = BlockHornCoral.properties.defaultState
            8 -> this.blockState = BlockDeadTubeCoral.properties.defaultState
            9 -> this.blockState = BlockDeadBrainCoral.properties.defaultState
            10 -> this.blockState = BlockDeadBubbleCoral.properties.defaultState
            11 -> this.blockState = BlockDeadFireCoral.properties.defaultState
            12 -> this.blockState = BlockDeadHornCoral.properties.defaultState
        }
    }

    private fun adjustName() {
        when (damage) {
            0 -> this.name = "Tube Coral"
            1 -> this.name = "Brain Coral"
            2 -> this.name = "Bubble Coral"
            3 -> this.name = "Fire Coral"
            4 -> this.name = "Horn Coral"
            8 -> this.name = "Dead Tube Coral"
            9 -> this.name = "Dead Brain Coral"
            10 -> this.name = "Dead Bubble Coral"
            11 -> this.name = "Dead Fire Coral"
            12 -> this.name = "Dead Horn Coral"
            else -> this.name = "Coral"
        }
    }
}