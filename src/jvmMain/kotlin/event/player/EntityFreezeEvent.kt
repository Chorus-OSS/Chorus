package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.entity.EntityEvent

class EntityFreezeEvent(entity: Entity) : EntityEvent(), Cancellable {
    init {
        this.entity = entity
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
