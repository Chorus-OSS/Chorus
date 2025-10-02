package org.chorus_oss.chorus.item


class ItemApple : ItemFood(ItemID.APPLE) {
    override val foodRestore: Int
        get() = 4

    override val saturationRestore: Float
        get() = 2.4f
}
