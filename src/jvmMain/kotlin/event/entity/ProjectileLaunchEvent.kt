package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class ProjectileLaunchEvent(entity: EntityProjectile, var shooter: Entity) : EntityEvent(), Cancellable {
    override var entity: Entity = entity

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
