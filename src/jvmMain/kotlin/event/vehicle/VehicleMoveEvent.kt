package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Transform

class VehicleMoveEvent(vehicle: Entity, val from: Transform, val to: Transform) :
    VehicleEvent(vehicle) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
