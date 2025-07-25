package org.chorus_oss.chorus.item

class ItemLeatherLeggings @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemColorArmor(ItemID.Companion.LEATHER_LEGGINGS, meta, count) {
    override val tier: Int
        get() = TIER_LEATHER

    override val isLeggings: Boolean
        get() = true

    override val armorPoints: Int
        get() = 2

    override val maxDurability: Int
        get() = 76
}