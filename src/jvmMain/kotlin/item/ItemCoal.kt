package org.chorus_oss.chorus.item


class ItemCoal @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.COAL, meta, count, "Coal") {
    init {
        if (this.meta == 1) {
            this.name = "Charcoal"
        }
    }

    val isCharcoal: Boolean
        get() = super.damage == 1
}
