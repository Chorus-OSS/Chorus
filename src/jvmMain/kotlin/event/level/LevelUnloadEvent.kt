package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Level


class LevelUnloadEvent(level: Level) : LevelEvent(level), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
