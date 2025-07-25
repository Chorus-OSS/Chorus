package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.form.response.Response
import org.chorus_oss.chorus.form.window.Form

class PlayerSettingsRespondedEvent(player: Player, formID: Int, window: Form<*>, response: Response) :
    PlayerEvent(), Cancellable {
    var formID: Int
        protected set
    var window: Form<*>
        protected set

    /**
     * Can be null if player closed the window instead of submitting it
     *
     * @return response
     */
    var response: Response
        protected set

    init {
        this.player = player
        this.formID = formID
        this.window = window
        this.response = response
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
