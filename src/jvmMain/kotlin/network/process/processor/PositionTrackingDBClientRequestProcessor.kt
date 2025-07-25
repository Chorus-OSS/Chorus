package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.nbt.tags.CompoundTag
import org.chorus_oss.protocol.packets.PositionTrackingDBClientRequestPacket
import java.io.IOException


class PositionTrackingDBClientRequestProcessor : PacketProcessor<PositionTrackingDBClientRequestPacket> {
    override fun handle(player: Player, packet: PositionTrackingDBClientRequestPacket) {
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

    override val packetID: Int = PositionTrackingDBClientRequestPacket.id

    companion object : Loggable
}
