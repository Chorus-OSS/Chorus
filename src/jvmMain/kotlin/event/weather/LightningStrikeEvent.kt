package org.chorus_oss.chorus.event.weather

import org.chorus_oss.chorus.entity.weather.EntityLightningStrike
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.level.WeatherEvent
import org.chorus_oss.chorus.level.Level

class LightningStrikeEvent(
    level: Level,
    /**
     * Gets the bolt which is striking the earth.
     * @return lightning entity
     */
    val lightning: EntityLightningStrike
) : WeatherEvent(level), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
