package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList

class VehicleDestroyByEntityEvent
/**
 * Constructor for the VehicleDestroyByEntityEvent
 *
 * @param vehicle   the destroyed vehicle
 * @param destroyer the destroying entity
 */(
    vehicle: Entity,
    /**
     * Returns the destroying entity
     *
     * @return destroying entity
     */
    val destroyer: Entity
) : VehicleDestroyEvent(vehicle) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
