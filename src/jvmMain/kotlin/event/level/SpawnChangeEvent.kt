package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator


class SpawnChangeEvent(level: Level, val previousSpawn: Locator) : LevelEvent(level) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
