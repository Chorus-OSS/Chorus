package org.chorus_oss.chorus.item

class ItemTropicalFish : ItemFish(ItemID.Companion.TROPICAL_FISH, 0, 1) {
    override val foodRestore: Int
        get() = 1

    override val saturationRestore: Float
        get() = 0.2f
}