package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.SetLocalPlayerAsInitializedPacket


class SetLocalPlayerAsInitializedPacketProcessor :
    DataPacketProcessor<MigrationPacket<SetLocalPlayerAsInitializedPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<SetLocalPlayerAsInitializedPacket>) {
        val player = player.player
        log.debug(
            "receive SetLocalPlayerAsInitializedPacket for {}",
            player.playerInfo.username
        )
        player.player.onPlayerLocallyInitialized()
    }

    override val packetId: Int = SetLocalPlayerAsInitializedPacket.id

    companion object : Loggable
}
