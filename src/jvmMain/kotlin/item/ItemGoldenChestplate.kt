package org.chorus_oss.chorus.item

class ItemGoldenChestplate @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.GOLDEN_CHESTPLATE, meta, count, "Golden Chestplate") {
    override val tier: Int
        get() = TIER_GOLD

    override val isChestplate: Boolean
        get() = true

    override val armorPoints: Int
        get() = 5

    override val maxDurability: Int
        get() = 113
}