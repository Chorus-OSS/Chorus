package org.chorus_oss.chorus.item


class ItemBread @JvmOverloads constructor(count: Int = 1) : ItemFood(ItemID.Companion.BREAD, 0, count) {
    override val foodRestore: Int
        get() = 5

    override val saturationRestore: Float
        get() = 6f
}
