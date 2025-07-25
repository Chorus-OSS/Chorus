package org.chorus_oss.chorus.item

class ItemIronHoe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.IRON_HOE, meta, count, "Iron Hoe") {
    override val maxDurability: Int
        get() = DURABILITY_IRON

    override val isHoe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_IRON
}