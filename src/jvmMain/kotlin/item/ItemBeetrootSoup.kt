package org.chorus_oss.chorus.item


class ItemBeetrootSoup @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.BEETROOT_SOUP, 0, count, "Beetroot Soup") {
    override val maxStackSize: Int
        get() = 1

    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 7.2f
}
