package org.chorus_oss.chorus.item


class ItemScrapNetherite @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.NETHERITE_SCRAP, 0, count, "Netherite Scrap") {
    override val isLavaResistant: Boolean
        get() = true
}
