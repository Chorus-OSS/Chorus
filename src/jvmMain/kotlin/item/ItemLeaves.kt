package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockBirchLeaves
import org.chorus_oss.chorus.block.BlockJungleLeaves
import org.chorus_oss.chorus.block.BlockOakLeaves
import org.chorus_oss.chorus.block.BlockSpruceLeaves

class ItemLeaves @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.LEAVES, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0 -> {
                name = "Oak Leaves"
                blockState = BlockOakLeaves.properties.defaultState
            }

            1 -> {
                name = "Spruce Leaves"
                blockState = BlockSpruceLeaves.properties.defaultState
                this.meta = 0
            }

            2 -> {
                name = "Birch Leaves"
                blockState = BlockBirchLeaves.properties.defaultState
                this.meta = 0
            }

            3 -> {
                name = "Jungle Leaves"
                blockState = BlockJungleLeaves.properties.defaultState
                this.meta = 0
            }

            else -> throw IllegalArgumentException("Invalid damage: $damage")
        }
    }
}