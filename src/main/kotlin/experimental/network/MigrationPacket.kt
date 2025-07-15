package org.chorus_oss.chorus.experimental.network

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketEncoder
import org.chorus_oss.chorus.network.PacketHandler
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.PacketCodec
import org.chorus_oss.protocol.core.PacketRegistry

data class MigrationPacket<T : Packet>(val packet: T) : DataPacket(), PacketEncoder {
    val codec: PacketCodec<T> = requireNotNull(PacketRegistry[packet]) {
        "PacketCodec not registered for $packet"
    }

    override fun pid(): Int = this.packet.id

    override fun handle(handler: PacketHandler) {
        handler.handle(this)
    }

    override fun encode(byteBuf: HandleByteBuf) {
        val buffer = Buffer()
        codec.serialize(this.packet, buffer)
        byteBuf.writeBytes(buffer.readByteArray())
    }
}