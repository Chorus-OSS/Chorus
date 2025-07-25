package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.lang.TextContainer

class PlayerKickEvent(player: Player, reason: Reason, reasonString: String, quitMessage: TextContainer) :
    PlayerEvent(), Cancellable {
    enum class Reason {
        NEW_CONNECTION,
        KICKED_BY_ADMIN,
        NOT_WHITELISTED,
        IP_BANNED,
        NAME_BANNED,
        INVALID_PVE,
        LOGIN_TIMEOUT,
        SERVER_FULL,
        FLYING_DISABLED,
        INVALID_PVP,

        UNKNOWN;

        override fun toString(): String {
            return this.name
        }
    }

    @JvmField
    var quitMessage: TextContainer

    val reasonEnum: Reason
    protected val reasonString: String

    constructor(player: Player, reason: Reason, quitMessage: TextContainer) : this(
        player,
        reason,
        reason.toString(),
        quitMessage
    )

    constructor(player: Player, reason: Reason, quitMessage: String) : this(
        player,
        reason,
        TextContainer(quitMessage)
    )

    init {
        this.player = player
        this.quitMessage = quitMessage
        this.reasonEnum = reason
        this.reasonString = reason.name
    }

    fun getReason(): String {
        return reasonString
    }

    fun setQuitMessage(joinMessage: String) {
        this.quitMessage = TextContainer(joinMessage)
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
