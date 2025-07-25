package org.chorus_oss.chorus.network.connection.netty.codec.packet

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper

class BedrockPacketCodecV1 : BedrockPacketCodec() {
    override fun encodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        buf.writeByte(msg.packetId and 0xff)
    }

    override fun decodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        msg.packetId = buf.readUnsignedByte().toInt()
    }
}
