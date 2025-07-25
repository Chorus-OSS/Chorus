package org.chorus_oss.chorus.dialog.window

import org.chorus_oss.chorus.Player

interface Dialog {
    fun send(player: Player)
}
