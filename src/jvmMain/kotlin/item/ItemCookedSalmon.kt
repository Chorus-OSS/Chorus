package org.chorus_oss.chorus.item

class ItemCookedSalmon : ItemSalmon(ItemID.Companion.COOKED_SALMON, 0, 1) {
    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 9.6f
}