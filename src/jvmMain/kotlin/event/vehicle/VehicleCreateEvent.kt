package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList


class VehicleCreateEvent(vehicle: Entity) : VehicleEvent(vehicle), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
