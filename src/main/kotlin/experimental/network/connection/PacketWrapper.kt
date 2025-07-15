package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.Sink
import kotlinx.io.Source
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.PacketRegistry
import org.chorus_oss.protocol.core.ProtoCodec

data class PacketWrapper(
    val header: PacketHeader,
    val packet: Packet
) {
    companion object : ProtoCodec<PacketWrapper> {
        override fun serialize(value: PacketWrapper, stream: Sink) {
            PacketHeader.serialize(value.header, stream)
            PacketRegistry[value.packet]?.serialize(value.packet, stream)
                ?: throw RuntimeException("PacketCodec not found for ${value.packet}")
        }

        override fun deserialize(stream: Source): PacketWrapper {
            val header: PacketHeader
            return PacketWrapper(
                header = PacketHeader.deserialize(stream).also { header = it },
                packet = PacketRegistry[header.packetID.toInt()]?.deserialize(stream) as? Packet
                    ?: throw RuntimeException("PacketCodec not found for id: ${header.packetID}")
            )
        }
    }
}
