package org.chorus_oss.chorus.item

class ItemLeatherBoots @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemColorArmor(ItemID.Companion.LEATHER_BOOTS, meta, count, "Leather Boots") {
    override val tier: Int
        get() = TIER_LEATHER

    override val isBoots: Boolean
        get() = true

    override val armorPoints: Int
        get() = 1

    override val maxDurability: Int
        get() = 66
}