package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.protocol.packets.AnimatePacket

class PlayerAnimationEvent : PlayerEvent, Cancellable {
    @JvmField
    val animationType: AnimatePacket.Action

    @JvmField
    val rowingTime: Float?

    constructor(player: Player, animatePacket: AnimatePacket) {
        this.player = player
        animationType = animatePacket.action
        rowingTime = when (animationType) {
            AnimatePacket.Action.RowLeft,
            AnimatePacket.Action.RowRight -> {
                val actionData = animatePacket.actionData as AnimatePacket.Action.RowingData
                actionData.rowingTime
            }

            else -> null
        }
    }

    @JvmOverloads
    constructor(player: Player, animation: AnimatePacket.Action = AnimatePacket.Action.SwingArm) {
        this.player = player
        this.animationType = animation
        rowingTime = 0f
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
