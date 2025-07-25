package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.vibration.VibrationListener

class VibrationArriveEvent(
    vibrationEvent: org.chorus_oss.chorus.level.vibration.VibrationEvent,
    protected var listener: VibrationListener
) : VibrationEvent(vibrationEvent) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
