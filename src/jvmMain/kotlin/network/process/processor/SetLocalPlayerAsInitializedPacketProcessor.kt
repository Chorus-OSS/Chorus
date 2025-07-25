package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.SetLocalPlayerAsInitializedPacket


class SetLocalPlayerAsInitializedPacketProcessor : PacketProcessor<SetLocalPlayerAsInitializedPacket> {
    override fun handle(player: Player, packet: SetLocalPlayerAsInitializedPacket) {

        log.debug(
            "receive SetLocalPlayerAsInitializedPacket for {}",
            player.playerInfo.username
        )
        player.player.onPlayerLocallyInitialized()
    }

    override val packetID: Int = SetLocalPlayerAsInitializedPacket.id

    companion object : Loggable
}
