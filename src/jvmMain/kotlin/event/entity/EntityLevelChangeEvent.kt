package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level

class EntityLevelChangeEvent(entity: Entity, originLevel: Level, targetLevel: Level) :
    EntityEvent(), Cancellable {
    val origin: Level
    val target: Level

    init {
        this.entity = entity
        this.origin = originLevel
        this.target = targetLevel
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
