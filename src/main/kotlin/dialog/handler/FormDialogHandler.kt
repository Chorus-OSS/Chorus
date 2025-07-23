package org.chorus_oss.chorus.dialog.handler

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.dialog.response.FormResponseDialog

fun interface FormDialogHandler {
    fun handle(player: Player, response: FormResponseDialog)
}
