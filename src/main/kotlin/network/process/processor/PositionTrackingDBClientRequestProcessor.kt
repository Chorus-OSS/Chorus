package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.nbt.tags.CompoundTag
import org.chorus_oss.protocol.packets.PositionTrackingDBClientRequestPacket
import java.io.IOException


class PositionTrackingDBClientRequestProcessor :
    DataPacketProcessor<MigrationPacket<PositionTrackingDBClientRequestPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<PositionTrackingDBClientRequestPacket>) {
        val packet = pk.packet

        val player = player.player
        try {
            val positionTracking =
                Server.instance.getPositionTrackingService().startTracking(player, packet.trackingID, true)
            if (positionTracking != null) {
                return
            }
        } catch (e: IOException) {
            log.warn(
                "Failed to track the trackingHandler {}",
                packet.trackingID,
                e
            )
        }
        val notFound = org.chorus_oss.protocol.packets.PositionTrackingDBServerBroadcastPacket(
            broadcastAction = org.chorus_oss.protocol.packets.PositionTrackingDBServerBroadcastPacket.Companion.Action.NotFound,
            trackingID = packet.trackingID,
            payload = CompoundTag()
        )
        player.sendPacket(notFound)
    }

    override val packetId: Int = PositionTrackingDBClientRequestPacket.id

    companion object : Loggable
}
