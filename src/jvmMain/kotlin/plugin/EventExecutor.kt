package org.chorus_oss.chorus.plugin

import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.Listener
import org.chorus_oss.chorus.utils.EventException

interface EventExecutor {
    @Throws(EventException::class)
    fun execute(listener: Listener?, event: Event)
}
