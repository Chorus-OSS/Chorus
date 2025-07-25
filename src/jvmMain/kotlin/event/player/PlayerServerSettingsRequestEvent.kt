package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.form.window.Form


class PlayerServerSettingsRequestEvent(player: Player, settings: MutableMap<Int, Form<*>>) :
    PlayerEvent(), Cancellable {
    private var settings: MutableMap<Int, Form<*>>

    init {
        this.player = player
        this.settings = settings
    }

    fun getSettings(): Map<Int, Form<*>> {
        return settings
    }

    fun setSettings(settings: MutableMap<Int, Form<*>>) {
        this.settings = settings
    }

    fun setSettings(id: Int, window: Form<*>) {
        settings[id] = window
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
