package org.chorus_oss.chorus.item

class ItemGoldenLeggings @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.GOLDEN_LEGGINGS, meta, count, "Golden Leggings") {
    override val tier: Int
        get() = TIER_GOLD

    override val isLeggings: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 106
}