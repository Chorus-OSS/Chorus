package org.chorus_oss.chorus.item

class ItemWoodenAxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.WOODEN_AXE, meta, count, "Wooden Axe") {
    override val maxDurability: Int
        get() = DURABILITY_WOODEN

    override val isAxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_WOODEN

    override val attackDamage: Int
        get() = 3

    override fun canBreakShield(): Boolean {
        return true
    }
}