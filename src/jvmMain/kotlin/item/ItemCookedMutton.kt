package org.chorus_oss.chorus.item

class ItemCookedMutton : ItemFood(ItemID.Companion.COOKED_MUTTON) {
    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 9.6f
}