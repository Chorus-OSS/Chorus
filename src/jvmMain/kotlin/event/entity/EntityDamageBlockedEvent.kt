package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause

class EntityDamageBlockedEvent(entity: Entity, damage: EntityDamageEvent, knockBack: Boolean, animation: Boolean) :
    EntityEvent(), Cancellable {
    val damage: EntityDamageEvent
    val knockBackAttacker: Boolean
    val animation: Boolean

    init {
        this.entity = entity
        this.damage = damage
        this.knockBackAttacker = knockBack
        this.animation = animation
    }

    val cause: DamageCause
        get() = damage.cause

    val attacker: Entity
        get() = damage.entity

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
