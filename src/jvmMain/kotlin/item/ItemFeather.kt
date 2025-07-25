package org.chorus_oss.chorus.item


class ItemFeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.FEATHER, 0, count, "Feather")
