package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.event.player.PlayerAnimationEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.AnimatePacket
import org.chorus_oss.protocol.packets.AnimatePacket.Action

class AnimateProcessor : DataPacketProcessor<MigrationPacket<AnimatePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<AnimatePacket>) {
        val packet = pk.packet

        if (!player.spawned || !player.isAlive()) {
            return
        }

        var animation = packet.action

        // prevent client send illegal packet to server and broadcast to other client and make other client crash
        if (animation == Action.WakeUp || animation == Action.CriticalHit || animation == Action.MagicCriticalHit
        ) {
            return
        }

        val animationEvent = PlayerAnimationEvent(player, packet)
        Server.instance.pluginManager.callEvent(animationEvent)
        if (animationEvent.cancelled) {
            return
        }
        animation = animationEvent.animationType

        when (animation) {
            Action.RowRight, Action.RowLeft -> {
                val actionData = packet.actionData as Action.RowingData
                val riding = player.riding
                if (riding is EntityBoat) {
                    riding.onPaddle(animation, actionData.rowingTime)
                }
                return
            }

            else -> Unit
        }

        if (animationEvent.animationType == Action.SwingArm) {
            player.setItemCoolDown(Player.NO_SHIELD_DELAY, "shield")
        }

        Server.broadcastPacket(
            player.viewers.values, AnimatePacket(
                targetRuntimeID = player.getRuntimeID().toULong(),
                action = animationEvent.animationType,
                actionData = null,
            )
        )
    }

    override val packetId: Int = AnimatePacket.id
}
