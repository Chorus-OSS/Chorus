package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.UUIDValidator


class EmoteProcessor : DataPacketProcessor<MigrationPacket<org.chorus_oss.protocol.packets.EmotePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<org.chorus_oss.protocol.packets.EmotePacket>) {
        val packet = pk.packet

        if (!player.player.spawned) {
            return
        }
        if (packet.actorRuntimeID != player.player.getRuntimeID().toULong()) {
            log.warn(
                "{} sent EmotePacket with invalid entity id: {} != {}",
                player.player.getEntityName(),
                packet.actorRuntimeID,
                player.player.getRuntimeID()
            )
            return
        }
        if (!UUIDValidator.isValidUUID(packet.emoteID)) {
            log.warn(
                "{} sent EmotePacket with invalid emoteId: {}",
                player.player.getEntityName(),
                packet.emoteID
            )
            return
        }

        for (viewer in player.player.viewers.values) {
            viewer.dataPacket(pk)
        }
    }

    override val packetId: Int = org.chorus_oss.protocol.packets.EmotePacket.id

    companion object : Loggable
}
