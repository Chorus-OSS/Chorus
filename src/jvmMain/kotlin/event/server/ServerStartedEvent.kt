package org.chorus_oss.chorus.event.server

import org.chorus_oss.chorus.event.HandlerList

/**
 * 服务器启动完毕后会触发，注意reload也会触发
 */
class ServerStartedEvent : ServerEvent() {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
