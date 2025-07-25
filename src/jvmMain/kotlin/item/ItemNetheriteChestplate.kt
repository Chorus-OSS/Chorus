package org.chorus_oss.chorus.item

class ItemNetheriteChestplate @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.NETHERITE_CHESTPLATE, meta, count, "Netherite Chestplate") {
    override val tier: Int
        get() = TIER_NETHERITE

    override val isChestplate: Boolean
        get() = true

    override val armorPoints: Int
        get() = 8

    override val maxDurability: Int
        get() = 592

    override val toughness: Int
        get() = 3

    override val isLavaResistant: Boolean
        get() = true
}