package org.chorus_oss.chorus.item

class ItemLeatherHelmet @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemColorArmor(ItemID.LEATHER_HELMET, meta, count) {

    override val tier: Int
        get() = TIER_LEATHER

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 1

    override val maxDurability: Int
        get() = 56
}