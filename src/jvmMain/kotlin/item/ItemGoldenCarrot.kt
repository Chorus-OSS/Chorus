package org.chorus_oss.chorus.item

class ItemGoldenCarrot : ItemFood(ItemID.Companion.GOLDEN_CARROT) {
    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 14.4f
}