package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

open class EntityCombustEvent(override var entity: Entity, @JvmField var duration: Int) : EntityEvent(), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
