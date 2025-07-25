package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.math.Vector3

class EntityMoveByPistonEvent(entity: Entity, pos: Vector3) :
    EntityMotionEvent(entity, pos) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
