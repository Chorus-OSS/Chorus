package org.chorus_oss.chorus.item

class ItemRabbit @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.RABBIT, meta, count, "Raw Rabbit") {
    override val foodRestore: Int
        get() = 3

    override val saturationRestore: Float
        get() = 1.8f
}