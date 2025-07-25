package org.chorus_oss.chorus.item


class ItemSnowball @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ProjectileItem(ItemID.Companion.SNOWBALL, 0, count, "Snowball") {
    override val maxStackSize: Int
        get() = 16

    override val projectileEntityType: String
        get() = ItemID.Companion.SNOWBALL

    override val throwForce: Float
        get() = 1.5f
}
