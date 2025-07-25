package org.chorus_oss.chorus.item


class ItemIngotNetherite @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.NETHERITE_INGOT, 0, count, "Netherite Ingot") {
    override val isLavaResistant: Boolean
        get() = true
}
