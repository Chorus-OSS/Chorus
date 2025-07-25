package org.chorus_oss.chorus.item

class ItemGoldenHoe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.GOLDEN_HOE, meta, count, "Golden Hoe") {
    override val maxDurability: Int
        get() = DURABILITY_GOLD

    override val isHoe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_GOLD
}