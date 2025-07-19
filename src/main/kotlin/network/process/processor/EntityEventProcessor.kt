package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.item.ItemFood
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.ActorEventPacket

class EntityEventProcessor : DataPacketProcessor<MigrationPacket<ActorEventPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<ActorEventPacket>) {
        val packet = pk.packet

        val player = player.player
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

    override val packetId: Int = ActorEventPacket.id
}
