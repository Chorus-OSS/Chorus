package org.chorus_oss.chorus.event.plugin

import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.plugin.Plugin


open class PluginEvent(val plugin: Plugin) : Event() {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
