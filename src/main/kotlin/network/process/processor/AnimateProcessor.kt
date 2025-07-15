package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.event.player.PlayerAnimationEvent
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.AnimatePacket
import org.chorus_oss.chorus.network.protocol.AnimatePacket.Action

class AnimateProcessor : DataPacketProcessor<AnimatePacket>() {
    override fun handle(player: Player, pk: AnimatePacket) {
        val player = player.player
        if (!player.spawned || !player.isAlive()) {
            return
        }

        var animation = pk.action

        // prevent client send illegal packet to server and broadcast to other client and make other client crash
        if (animation == Action.WAKE_UP || animation == Action.CRITICAL_HIT || animation == Action.MAGIC_CRITICAL_HIT
        ) {
            return
        }

        val animationEvent = PlayerAnimationEvent(player, pk)
        Server.instance.pluginManager.callEvent(animationEvent)
        if (animationEvent.cancelled) {
            return
        }
        animation = animationEvent.animationType

        when (animation) {
            Action.ROW_RIGHT, Action.ROW_LEFT -> {
                val actionData = pk.actionData as Action.RowingData
                val riding = player.riding
                if (riding is EntityBoat) {
                    riding.onPaddle(animation, actionData.rowingTime)
                }
                return
            }

            else -> Unit
        }

        if (animationEvent.animationType == Action.SWING_ARM) {
            player.setItemCoolDown(Player.NO_SHIELD_DELAY, "shield")
        }

        Server.broadcastPacket(
            player.viewers.values, AnimatePacket(
                targetRuntimeID = player.getRuntimeID(),
                action = animationEvent.animationType,
                actionData = null,
            )
        )
    }

    override val packetId: Int
        get() = ProtocolInfo.ANIMATE_PACKET
}
