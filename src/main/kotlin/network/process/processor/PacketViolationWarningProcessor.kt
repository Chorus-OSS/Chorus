package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.PacketRegistry


class PacketViolationWarningProcessor :
    DataPacketProcessor<MigrationPacket<org.chorus_oss.protocol.packets.PacketViolationWarningPacket>>() {
    override fun handle(
        player: Player,
        pk: MigrationPacket<org.chorus_oss.protocol.packets.PacketViolationWarningPacket>
    ) {
        val packet = pk.packet

        val codecName = PacketRegistry[packet.packetID]?.let {
            it::class.simpleName
        }

        log.warn(
            "PacketViolationWarning from ${player.senderName} for ${
                codecName?.let { "codec $it" } ?: "id ${packet.packetID}"
            }: $pk"
        )
    }

    override val packetId: Int = org.chorus_oss.protocol.packets.PacketViolationWarningPacket.id

    companion object : Loggable
}
