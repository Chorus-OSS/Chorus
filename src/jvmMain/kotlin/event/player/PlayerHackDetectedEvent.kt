package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList

class PlayerHackDetectedEvent(player: Player, type: HackType) : PlayerEvent() {
    var isKick: Boolean = true
    protected var type: HackType

    init {
        this.player = player
        this.type = type
    }

    enum class HackType {
        COMMAND_SPAM,
        PERMISSION_REQUEST,
        INVALID_PVP,
        INVALID_PVE
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
