package org.chorus_oss.chorus.item


class ItemElytra @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemArmor(ItemID.Companion.ELYTRA, meta, count, "Elytra") {
    override val maxDurability: Int
        get() = 433

    override val isArmor: Boolean
        get() = false

    override val isChestplate: Boolean
        get() = true
}
