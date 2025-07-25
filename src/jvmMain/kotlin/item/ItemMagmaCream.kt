package org.chorus_oss.chorus.item

class ItemMagmaCream(meta: Int, count: Int) :
    Item(ItemID.Companion.MAGMA_CREAM, meta, count, "Magma Cream") {
    @JvmOverloads
    constructor(meta: Int? = 0) : this(0, 1)
}
