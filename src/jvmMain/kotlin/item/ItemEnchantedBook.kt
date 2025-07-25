package org.chorus_oss.chorus.item

open class ItemEnchantedBook : Item {
    constructor() : super(ItemID.Companion.ENCHANTED_BOOK)

    protected constructor(id: String) : super(id)

    override val maxStackSize: Int
        get() = 1

    override fun applyEnchantments(): Boolean {
        return false
    }
}