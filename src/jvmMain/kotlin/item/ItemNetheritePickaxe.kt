package org.chorus_oss.chorus.item

class ItemNetheritePickaxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.NETHERITE_PICKAXE, meta, count, "Netherite Pickaxe") {
    override val maxDurability: Int
        get() = DURABILITY_NETHERITE

    override val isPickaxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_NETHERITE

    override val attackDamage: Int
        get() = 6

    override val isLavaResistant: Boolean
        get() = true
}