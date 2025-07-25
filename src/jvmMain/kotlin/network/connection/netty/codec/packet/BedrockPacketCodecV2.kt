package org.chorus_oss.chorus.network.connection.netty.codec.packet

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper

class BedrockPacketCodecV2 : BedrockPacketCodec() {
    override fun encodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        buf.writeByte(msg.packetId)
        buf.writeByte(msg.senderSubClientId)
        buf.writeByte(msg.targetSubClientId)
    }

    override fun decodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        msg.packetId = buf.readUnsignedByte().toInt()
        msg.senderSubClientId = buf.readUnsignedByte().toInt()
        msg.targetSubClientId = buf.readUnsignedByte().toInt()
    }
}
