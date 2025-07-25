package org.chorus_oss.chorus.item

class ItemWoodenPickaxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.WOODEN_PICKAXE, meta, count, "Wooden Pickaxe") {
    override val maxDurability: Int
        get() = DURABILITY_WOODEN

    override val isPickaxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_WOODEN

    override val attackDamage: Int
        get() = 2
}