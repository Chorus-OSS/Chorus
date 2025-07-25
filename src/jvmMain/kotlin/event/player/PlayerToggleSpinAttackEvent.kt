package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerToggleSpinAttackEvent(player: Player, isSpinAttacking: Boolean) : PlayerEvent(), Cancellable {
    val isSpinAttacking: Boolean

    init {
        this.player = player
        this.isSpinAttacking = isSpinAttacking
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
