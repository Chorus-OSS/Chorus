package org.chorus_oss.chorus.item

class ItemGoldenBoots @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.GOLDEN_BOOTS, meta, count, "Golden Boots") {
    override val tier: Int
        get() = TIER_GOLD

    override val isBoots: Boolean
        get() = true

    override val armorPoints: Int
        get() = 1

    override val maxDurability: Int
        get() = 92
}