package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList


class EntityDamageByChildEntityEvent(
    damager: Entity, val child: Entity, entity: Entity, cause: DamageCause, damage: Float
) : EntityDamageByEntityEvent(damager, entity, cause, damage) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
