package org.chorus_oss.chorus.item

class ItemMelonSlice @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.MELON_SLICE, meta, count, "Melon Slice") {
    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 1.2f
}