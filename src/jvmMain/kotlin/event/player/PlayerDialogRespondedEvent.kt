package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.dialog.response.FormResponseDialog
import org.chorus_oss.chorus.dialog.window.FormWindowDialog
import org.chorus_oss.chorus.event.HandlerList

class PlayerDialogRespondedEvent(player: Player?, var dialog: FormWindowDialog, var response: FormResponseDialog) :
    PlayerEvent() {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
