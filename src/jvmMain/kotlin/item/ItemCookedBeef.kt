package org.chorus_oss.chorus.item

class ItemCookedBeef : ItemFood(ItemID.COOKED_BEEF) {
    override val foodRestore: Int
        get() = 8

    override val saturationRestore: Float
        get() = 12.8f
}