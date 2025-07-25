package org.chorus_oss.chorus.event.vehicle

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Event

abstract class VehicleEvent(val vehicle: Entity) : Event()
