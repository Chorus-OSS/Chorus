package org.chorus_oss.chorus.item

class ItemBeef : ItemFood(ItemID.Companion.BEEF) {
    override val foodRestore: Int
        get() = 3

    override val saturationRestore: Float
        get() = 1.8f
}