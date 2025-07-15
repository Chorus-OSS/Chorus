package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor

class ShowCreditsProcessor : DataPacketProcessor<MigrationPacket<org.chorus_oss.protocol.packets.ShowCreditsPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<org.chorus_oss.protocol.packets.ShowCreditsPacket>) {
        val packet = pk.packet

        if (packet.statusType == org.chorus_oss.protocol.packets.ShowCreditsPacket.Companion.StatusType.End) {
            if (player.player.showingCredits) {
                player.player.showingCredits = false
                player.player.teleport(
                    player.player.spawn.first!!,
                    PlayerTeleportEvent.TeleportCause.END_PORTAL
                )
            }
        }
    }

    override val packetId: Int = org.chorus_oss.protocol.packets.ShowCreditsPacket.id
}
