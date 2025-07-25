package org.chorus_oss.chorus.item

class ItemMutton @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.MUTTON, meta, count, "Raw Mutton") {
    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 1.2f
}