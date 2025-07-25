package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class ExplosionPrimeEvent(entity: Entity, force: Double) : EntityEvent(), Cancellable {
    var force: Double
    var isBlockBreaking: Boolean

    init {
        this.entity = entity
        this.force = force
        this.isBlockBreaking = true
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
