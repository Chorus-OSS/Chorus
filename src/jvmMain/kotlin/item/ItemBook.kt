package org.chorus_oss.chorus.item


class ItemBook : Item(ItemID.Companion.BOOK) {
    override val enchantAbility: Int
        get() = 1
}
