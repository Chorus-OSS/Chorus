package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level

class ThunderChangeEvent(level: Level, private val to: Boolean) : WeatherEvent(level), Cancellable {
    /**
     * Gets the state of thunder that the world is being set to
     *
     * @return true if the thunder is being set to start, false otherwise
     */
    fun toThunderState(): Boolean {
        return to
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
