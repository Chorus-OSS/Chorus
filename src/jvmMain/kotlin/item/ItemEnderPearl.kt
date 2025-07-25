package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.throwable.EntityEnderPearl

class ItemEnderPearl @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ProjectileItem(ItemID.Companion.ENDER_PEARL, 0, count, "Ender Pearl") {
    override val maxStackSize: Int
        get() = 16

    override val projectileEntityType: String
        get() = ItemID.Companion.ENDER_PEARL

    override val throwForce: Float
        get() = 1.5f

    override fun correctProjectile(player: Player, projectile: Entity): Entity? {
        if (projectile is EntityEnderPearl) {
            if (!player.isItemCoolDownEnd(this.identifier)) {
                projectile.kill()
                return null
            }
            player.setItemCoolDown(20, this.identifier)
            return projectile
        }
        return null
    }
}
