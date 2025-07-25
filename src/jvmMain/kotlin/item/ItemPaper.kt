package org.chorus_oss.chorus.item


class ItemPaper @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.PAPER, meta, count, "Paper")
