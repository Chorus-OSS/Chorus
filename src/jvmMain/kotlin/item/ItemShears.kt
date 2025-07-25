package org.chorus_oss.chorus.item


class ItemShears @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.SHEARS, meta, count, "Shears") {
    override val maxDurability: Int
        get() = DURABILITY_SHEARS

    override val isShears: Boolean
        get() = true
}
