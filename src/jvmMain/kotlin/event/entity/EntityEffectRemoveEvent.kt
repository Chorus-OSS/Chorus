package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityEffectRemoveEvent(entity: Entity, effect: Effect) : EntityEvent(),
    Cancellable {
    val removeEffect: Effect

    init {
        this.entity = entity
        this.removeEffect = effect
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
