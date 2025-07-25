package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityExitVehicleEvent(val entity: Entity, vehicle: Entity) : VehicleEvent(vehicle),
    Cancellable {
    val isPlayer: Boolean
        get() = entity is Player

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
