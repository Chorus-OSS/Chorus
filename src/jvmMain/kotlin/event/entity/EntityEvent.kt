package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Event

abstract class EntityEvent : Event() {
    open lateinit var entity: Entity
        protected set
}
