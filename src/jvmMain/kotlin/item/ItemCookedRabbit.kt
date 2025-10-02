package org.chorus_oss.chorus.item

class ItemCookedRabbit : ItemFood(ItemID.COOKED_RABBIT) {
    override val foodRestore: Int
        get() = 5

    override val saturationRestore: Float
        get() = 6f
}