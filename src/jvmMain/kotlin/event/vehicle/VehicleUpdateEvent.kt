package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList

class VehicleUpdateEvent(vehicle: Entity) : VehicleEvent(vehicle) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
