package org.chorus_oss.chorus.item

class ItemSaddle @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.SADDLE, meta, count, "Saddle") {
    override val maxStackSize: Int
        get() = 1
}
