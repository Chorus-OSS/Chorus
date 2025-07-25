package org.chorus_oss.chorus.level.vibration


interface VibrationManager {
    fun callVibrationEvent(event: VibrationEvent)

    fun addListener(listener: VibrationListener)

    fun removeListener(listener: VibrationListener)
}
