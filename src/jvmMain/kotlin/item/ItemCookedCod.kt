package org.chorus_oss.chorus.item

class ItemCookedCod : ItemCod(ItemID.Companion.COOKED_COD, 0, 1) {
    override val foodRestore: Int
        get() = 5

    override val saturationRestore: Float
        get() = 6f
}