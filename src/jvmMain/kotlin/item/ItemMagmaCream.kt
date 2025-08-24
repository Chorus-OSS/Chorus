package org.chorus_oss.chorus.item

class ItemMagmaCream(meta: Int, count: Int) :
    Item(ItemID.MAGMA_CREAM, meta, count, "Magma Cream") {
    @JvmOverloads
    constructor(meta: Int? = 0) : this(0, 1)
}
