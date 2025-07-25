package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.level.vibration.VibrationEvent

abstract class VibrationEvent(var vibrationEvent: VibrationEvent) : Event(), Cancellable
