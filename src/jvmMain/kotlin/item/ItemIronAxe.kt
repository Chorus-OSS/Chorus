package org.chorus_oss.chorus.item

class ItemIronAxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.IRON_AXE, meta, count, "Iron Axe") {
    override val maxDurability: Int
        get() = DURABILITY_IRON

    override val isAxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_IRON

    override val attackDamage: Int
        get() = 5

    override fun canBreakShield(): Boolean {
        return true
    }
}