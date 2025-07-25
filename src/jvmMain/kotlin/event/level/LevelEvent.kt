package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.level.Level


abstract class LevelEvent(val level: Level) : Event()
