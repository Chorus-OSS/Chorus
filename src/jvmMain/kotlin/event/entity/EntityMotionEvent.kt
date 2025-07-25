package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.math.Vector3

open class EntityMotionEvent(override var entity: Entity, val motion: Vector3) : EntityEvent(), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
