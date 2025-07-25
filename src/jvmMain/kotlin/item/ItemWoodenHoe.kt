package org.chorus_oss.chorus.item

class ItemWoodenHoe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.WOODEN_HOE, meta, count, "Wooden Hoe") {
    override val maxDurability: Int
        get() = DURABILITY_WOODEN

    override val isHoe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_WOODEN
}