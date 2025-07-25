package org.chorus_oss.chorus.event.server

import org.chorus_oss.chorus.event.HandlerList

class ServerStopEvent : ServerEvent() {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
