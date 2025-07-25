package org.chorus_oss.chorus.network.connection.netty.codec.packet

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper
import org.chorus_oss.chorus.utils.ByteBufVarInt

class BedrockPacketCodecV3 : BedrockPacketCodec() {
    override fun encodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        var header = 0
        header = header or (msg.packetId and 0x3ff)
        header = header or ((msg.senderSubClientId and 3) shl 10)
        header = header or ((msg.targetSubClientId and 3) shl 12)
        ByteBufVarInt.writeUnsignedInt(buf, header)
    }

    override fun decodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        val header = ByteBufVarInt.readUnsignedInt(buf)
        msg.packetId = header and 0x3ff
        msg.senderSubClientId = (header shr 10) and 3
        msg.targetSubClientId = (header shr 12) and 3
    }
}
