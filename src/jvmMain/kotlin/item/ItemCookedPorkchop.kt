package org.chorus_oss.chorus.item

class ItemCookedPorkchop : ItemFood(ItemID.Companion.COOKED_PORKCHOP) {
    override val foodRestore: Int
        get() = 8

    override val saturationRestore: Float
        get() = 12.8f
}