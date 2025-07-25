package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList


class EntityCombustByEntityEvent(val combusterEntity: Entity, combustingEntity: Entity, duration: Int) :
    EntityCombustEvent(combustingEntity, duration) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
