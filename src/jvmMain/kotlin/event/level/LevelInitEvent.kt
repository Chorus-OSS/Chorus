package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level


class LevelInitEvent(level: Level) : LevelEvent(level) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
