package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.MovingObjectPosition

class ProjectileHitEvent @JvmOverloads constructor(
    entity: EntityProjectile,
    movingObjectPosition: MovingObjectPosition? = null
) :
    EntityEvent(), Cancellable {
    var movingObjectPosition: MovingObjectPosition?

    init {
        this.entity = entity
        this.movingObjectPosition = movingObjectPosition
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
