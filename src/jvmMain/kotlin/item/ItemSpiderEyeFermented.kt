package org.chorus_oss.chorus.item

class ItemSpiderEyeFermented(meta: Int, count: Int) :
    Item(ItemID.Companion.FERMENTED_SPIDER_EYE, meta, count, "Fermented Spider Eye") {
    @JvmOverloads
    constructor(meta: Int? = 0) : this(0, 1)
}
