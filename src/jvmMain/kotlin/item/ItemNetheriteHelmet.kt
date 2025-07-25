package org.chorus_oss.chorus.item

class ItemNetheriteHelmet @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.NETHERITE_HELMET, meta, count, "Netherite Helmet") {
    override val tier: Int
        get() = TIER_NETHERITE

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 407

    override val toughness: Int
        get() = 3

    override val isLavaResistant: Boolean
        get() = true
}