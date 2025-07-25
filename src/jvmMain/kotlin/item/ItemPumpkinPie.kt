package org.chorus_oss.chorus.item


class ItemPumpkinPie @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.PUMPKIN_PIE, meta, count, "Pumpkin Pie") {
    override val foodRestore: Int
        get() = 8

    override val saturationRestore: Float
        get() = 4.8f
}
