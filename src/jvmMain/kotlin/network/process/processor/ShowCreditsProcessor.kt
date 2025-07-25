package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.ShowCreditsPacket

class ShowCreditsProcessor : PacketProcessor<ShowCreditsPacket> {
    override fun handle(player: Player, packet: ShowCreditsPacket) {
        if (packet.statusType == ShowCreditsPacket.Companion.StatusType.End) {
            if (player.showingCredits) {
                player.showingCredits = false
                player.teleport(
                    player.spawn.first!!,
                    PlayerTeleportEvent.TeleportCause.END_PORTAL
                )
            }
        }
    }

    override val packetID: Int = ShowCreditsPacket.id
}
