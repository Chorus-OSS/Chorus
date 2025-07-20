package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.item.ItemFood
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.ActorEventPacket

class EntityEventProcessor : PacketProcessor<ActorEventPacket> {
    override fun handle(player: Player, packet: ActorEventPacket) {

        if (!player.spawned || !player.isAlive()) {
            return
        }

        if (packet.eventType == ActorEventPacket.Companion.Type.EatingItem) {
            if (packet.eventData == 0 || packet.actorRuntimeID != player.getRuntimeID().toULong()) {
                return
            }

            val hand = player.inventory.itemInHand as? ItemFood ?: return

            val predictedData = (hand.runtimeId shl 16) or hand.damage
            if (packet.eventData != predictedData) {
                return
            }

            val pk = ActorEventPacket(
                actorRuntimeID = player.getRuntimeID().toULong(),
                eventType = ActorEventPacket.Companion.Type.EatingItem,
                eventData = predictedData,
            )
            player.sendPacket(pk)
            Server.broadcastPacket(player.viewers.values, pk)
        } else if (packet.eventType == ActorEventPacket.Companion.Type.Enchant) {
            if (packet.actorRuntimeID != player.getRuntimeID().toULong()) {
                return
            }
        }
    }

    override val packetID: Int = ActorEventPacket.id
}
