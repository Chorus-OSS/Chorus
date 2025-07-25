package org.chorus_oss.chorus.item


class ItemIngotGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.GOLD_INGOT, 0, count, "Gold Ingot")
