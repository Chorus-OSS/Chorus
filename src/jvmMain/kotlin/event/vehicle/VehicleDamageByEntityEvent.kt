package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityVehicle
import org.chorus_oss.chorus.event.HandlerList

class VehicleDamageByEntityEvent
/**
 * Constructor for the VehicleDamageByEntityEvent
 *
 * @param vehicle  the damaged vehicle
 * @param attacker the attacking vehicle
 * @param damage   the caused damage on the vehicle
 */(
    vehicle: EntityVehicle,
    /**
     * Returns the attacking entity
     *
     * @return attacking entity
     */
    val attacker: Entity, damage: Double
) :
    VehicleDamageEvent(vehicle, damage) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
