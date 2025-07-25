package org.chorus_oss.chorus.item

class ItemNetheriteAxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.NETHERITE_AXE, meta, count, "Netherite Axe") {
    override val maxDurability: Int
        get() = DURABILITY_NETHERITE

    override val isAxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_NETHERITE

    override val attackDamage: Int
        get() = 8

    override val isLavaResistant: Boolean
        get() = true

    override fun canBreakShield(): Boolean {
        return true
    }
}