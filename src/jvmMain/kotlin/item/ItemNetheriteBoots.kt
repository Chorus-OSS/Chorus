package org.chorus_oss.chorus.item

class ItemNetheriteBoots @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.NETHERITE_BOOTS, meta, count, "Netherite Boots") {
    override val tier: Int
        get() = TIER_NETHERITE

    override val isBoots: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 481

    override val toughness: Int
        get() = 3

    override val isLavaResistant: Boolean
        get() = true
}