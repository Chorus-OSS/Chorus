package org.chorus_oss.chorus.item


class ItemClock @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.CLOCK, meta, count, "Clock")
