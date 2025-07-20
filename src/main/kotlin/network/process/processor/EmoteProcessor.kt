package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.UUIDValidator
import org.chorus_oss.protocol.packets.EmotePacket


class EmoteProcessor : PacketProcessor<EmotePacket> {
    override fun handle(player: Player, packet: EmotePacket) {
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
            viewer.sendPacket(packet)
        }
    }

    override val packetID: Int = EmotePacket.id

    companion object : Loggable
}
