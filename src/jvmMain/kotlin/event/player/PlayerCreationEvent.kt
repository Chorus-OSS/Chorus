package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.HandlerList


class PlayerCreationEvent(@JvmField var playerClass: Class<out Player>) : Event() {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
