package org.chorus_oss.chorus.item

class ItemStoneHoe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.STONE_HOE, meta, count, "Stone Hoe") {
    override val maxDurability: Int
        get() = DURABILITY_STONE

    override val isHoe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_STONE
}