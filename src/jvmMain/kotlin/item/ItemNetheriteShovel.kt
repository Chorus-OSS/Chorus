package org.chorus_oss.chorus.item

class ItemNetheriteShovel @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.NETHERITE_SHOVEL, meta, count, "Netherite Shovel") {
    override val maxDurability: Int
        get() = DURABILITY_NETHERITE

    override val isShovel: Boolean
        get() = true

    override val tier: Int
        get() = TIER_NETHERITE

    override val attackDamage: Int
        get() = 5

    override val isLavaResistant: Boolean
        get() = true
}