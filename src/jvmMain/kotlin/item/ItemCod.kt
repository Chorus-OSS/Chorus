package org.chorus_oss.chorus.item

/**
 * ItemFish
 */
open class ItemCod : ItemFish {
    constructor() : super(ItemID.Companion.COD, 0, 1)

    protected constructor(id: String, meta: Int, count: Int) : super(id, meta, count)

    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 0.4f
}