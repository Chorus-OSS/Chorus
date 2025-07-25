package org.chorus_oss.chorus.item

class ItemNetheriteLeggings @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.NETHERITE_LEGGINGS, meta, count, "Netherite Leggings") {
    override val isLeggings: Boolean
        get() = true

    override val tier: Int
        get() = TIER_NETHERITE

    override val armorPoints: Int
        get() = 6

    override val maxDurability: Int
        get() = 555

    override val toughness: Int
        get() = 3

    override val isLavaResistant: Boolean
        get() = true
}