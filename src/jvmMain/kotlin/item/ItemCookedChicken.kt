package org.chorus_oss.chorus.item

class ItemCookedChicken : ItemFood(ItemID.Companion.COOKED_CHICKEN) {
    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 7.2f
}