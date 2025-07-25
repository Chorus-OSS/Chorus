package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.HandlerList

class VibrationOccurEvent(vibrationEvent: org.chorus_oss.chorus.level.vibration.VibrationEvent) :
    VibrationEvent(vibrationEvent) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
