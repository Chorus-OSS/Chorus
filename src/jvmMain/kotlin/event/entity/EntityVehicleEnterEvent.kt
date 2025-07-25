package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityVehicleEnterEvent(entity: Entity, vehicle: Entity) : EntityEvent(), Cancellable {
    val vehicle: Entity

    init {
        this.entity = entity
        this.vehicle = vehicle
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
