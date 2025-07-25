package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityEffectUpdateEvent(entity: Entity, oldEffect: Effect, newEffect: Effect) :
    EntityEvent(), Cancellable {
    val oldEffect: Effect
    val newEffect: Effect

    init {
        this.entity = entity
        this.oldEffect = oldEffect
        this.newEffect = newEffect
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
