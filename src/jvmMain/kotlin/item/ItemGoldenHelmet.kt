package org.chorus_oss.chorus.item

class ItemGoldenHelmet @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.GOLDEN_HELMET, meta, count, "Golden Helmet") {
    override val tier: Int
        get() = TIER_GOLD

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 2

    override val maxDurability: Int
        get() = 78
}