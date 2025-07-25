package org.chorus_oss.chorus.item

class ItemSpyglass @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.SPYGLASS, meta, count, "Spyglass") {
    override val maxStackSize: Int
        get() = 1
}
