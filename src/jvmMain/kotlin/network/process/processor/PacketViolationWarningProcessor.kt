package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.PacketRegistry
import org.chorus_oss.protocol.packets.PacketViolationWarningPacket


class PacketViolationWarningProcessor : PacketProcessor<PacketViolationWarningPacket> {
    override fun handle(
        player: Player,
        packet: PacketViolationWarningPacket
    ) {
        val codecName = PacketRegistry[packet.packetID]?.let {
            it::class.simpleName
        }

        log.warn(
            "PacketViolationWarning from ${player.senderName} for ${
                codecName?.let { "codec $it" } ?: "id ${packet.packetID}"
            }: $packet"
        )
    }

    override val packetID: Int = PacketViolationWarningPacket.id

    companion object : Loggable
}
