package org.chorus_oss.chorus.event.plugin

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.plugin.Plugin


class PluginDisableEvent(plugin: Plugin) : PluginEvent(plugin) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
