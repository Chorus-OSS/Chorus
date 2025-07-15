package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.Sink
import kotlinx.io.Source
import org.chorus_oss.protocol.core.ProtoCodec
import org.chorus_oss.protocol.core.ProtoVAR
import org.chorus_oss.protocol.core.types.UInt

data class PacketHeader(
    val packetID: UShort,
    val senderSubClientID: UByte,
    val targetSubClientID: UByte,
) {
    companion object : ProtoCodec<PacketHeader> {
        override fun serialize(value: PacketHeader, stream: Sink) {
            var header = 0u
            header = header or ((value.packetID and 0x3ffu).toUInt())
            header = header or ((value.senderSubClientID and 3u).toUInt() shl 10)
            header = header or ((value.targetSubClientID and 3u).toUInt() shl 12)
            ProtoVAR.UInt.serialize(header, stream)
        }

        override fun deserialize(stream: Source): PacketHeader {
            val header = ProtoVAR.UInt.deserialize(stream)
            return PacketHeader(
                packetID = (header and 0x3ffu).toUShort(),
                senderSubClientID = ((header shr 10) and 0x3u).toUByte(),
                targetSubClientID = ((header shr 12) and 0x3u).toUByte(),
            )
        }
    }
}
