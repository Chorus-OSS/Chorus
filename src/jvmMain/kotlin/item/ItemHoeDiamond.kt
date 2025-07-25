package org.chorus_oss.chorus.item


class ItemHoeDiamond @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.DIAMOND_HOE, meta, count, "Diamond Hoe") {
    override val maxDurability: Int
        get() = DURABILITY_DIAMOND

    override val isHoe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND
}
