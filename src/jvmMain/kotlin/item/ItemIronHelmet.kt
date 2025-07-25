package org.chorus_oss.chorus.item

class ItemIronHelmet @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.IRON_HELMET, meta, count, "Iron Helmet") {
    override val tier: Int
        get() = TIER_IRON

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 2

    override val maxDurability: Int
        get() = 166
}