package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.lang.TextContainer

class PlayerQuitEvent @JvmOverloads constructor(
    player: Player,
    quitMessage: TextContainer,
    autoSave: Boolean = true,
    reason: String = "No reason"
) :
    PlayerEvent() {
    @JvmField
    var quitMessage: TextContainer? = null

    @JvmField
    var autoSave: Boolean = true
    var reason: String? = null
        protected set

    constructor(player: Player, quitMessage: TextContainer, reason: String) : this(player, quitMessage, true, reason)

    constructor(player: Player, quitMessage: String, reason: String) : this(player, quitMessage, true, reason)

    constructor(player: Player, quitMessage: String, autoSave: Boolean, reason: String) : this(
        player,
        TextContainer(quitMessage),
        autoSave,
        reason
    )

    @JvmOverloads
    constructor(player: Player, quitMessage: String, autoSave: Boolean = true) : this(
        player,
        TextContainer(quitMessage),
        autoSave
    )

    init {
        this.player = player
        this.quitMessage = quitMessage
        this.autoSave = autoSave
        this.reason = reason
    }

    fun setQuitMessage(quitMessage: String) {
        this.quitMessage = TextContainer(quitMessage)
    }

    fun setAutoSave() {
        this.autoSave = true
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
