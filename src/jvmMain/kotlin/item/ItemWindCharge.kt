package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.EntityID

class ItemWindCharge @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ProjectileItem(ItemID.Companion.WIND_CHARGE, 0, count, "Wind Charge") {
    override val maxStackSize: Int
        get() = 64

    override val projectileEntityType: String
        get() = EntityID.WIND_CHARGE_PROJECTILE

    override val throwForce: Float
        get() = 1.5f
}