package org.chorus_oss.chorus.item

class ItemLeatherChestplate @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemColorArmor(ItemID.Companion.LEATHER_CHESTPLATE, meta, count) {
    override val tier: Int
        get() = TIER_LEATHER

    override val isChestplate: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 81
}