package org.chorus_oss.chorus.item

class ItemDriedKelp @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.DRIED_KELP, 0, count, "Dried Kelp") {
    override val foodRestore: Int
        get() = 1

    override val saturationRestore: Float
        get() = 0.6f

    override val eatingTicks: Int
        get() = 17
}
