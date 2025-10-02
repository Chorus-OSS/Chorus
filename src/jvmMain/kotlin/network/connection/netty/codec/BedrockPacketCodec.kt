package org.chorus_oss.chorus.network.connection.netty.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper
import org.chorus_oss.chorus.utils.ByteBufVarInt
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.PacketRegistry

class BedrockPacketCodec : MessageToMessageCodec<ByteBuf, BedrockPacketWrapper>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: BedrockPacketWrapper, out: MutableList<Any>) {
        if (msg.packetBuffer != null) {
            // We have a pre-encoded packet buffer, just use that.
            out.add(msg.retain())
        } else {
            val buf = ctx.alloc().buffer(128)
            try {
                val packet = msg.packet
                msg.packetId = packet!!.id
                encodeHeader(buf, msg)
                buf.writeBytes(
                    Buffer().apply {
                        requireNotNull(PacketRegistry[packet]) { "PacketCodec not registered for $packet" }.serialize(
                            packet,
                            this
                        )
                    }.readByteArray()
                )
                msg.packetBuffer = buf.retain()
                out.add(msg.retain())
            } catch (t: Throwable) {
                log.error("Error encoding packet {}", msg.packet, t)
            } finally {
                buf.release()
            }
        }
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val wrapper = BedrockPacketWrapper(0, 0, 0, null, null)
        wrapper.packetBuffer = msg.retainedSlice()
        try {
            val index = msg.readerIndex()
            this.decodeHeader(msg, wrapper)
            wrapper.headerLength = msg.readerIndex() - index

            val codec = PacketRegistry[wrapper.packetId]
            if (codec == null) {
                log.info("Codec not found for packet with ID: ${wrapper.packetId}")
                return
            }

            wrapper.packet = codec.deserialize(Buffer().apply {
                val bytes = ByteArray(msg.readableBytes())
                msg.readBytes(bytes)
                write(bytes)
            }) as Packet

            out.add(wrapper.retain())
        } catch (t: Throwable) {
            log.info("Failed to decode PacketID: ${wrapper.packetId}", t)
            throw t
        } finally {
            wrapper.release()
        }
    }

    fun encodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        var header = 0
        header = header or (msg.packetId and 0x3ff)
        header = header or ((msg.senderSubClientId and 3) shl 10)
        header = header or ((msg.targetSubClientId and 3) shl 12)
        ByteBufVarInt.writeUnsignedInt(buf, header)
    }

    fun decodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper) {
        val header = ByteBufVarInt.readUnsignedInt(buf)
        msg.packetId = header and 0x3ff
        msg.senderSubClientId = (header shr 10) and 3
        msg.targetSubClientId = (header shr 12) and 3
    }

    companion object : Loggable {
        const val NAME: String = "bedrock-packet-codec"
    }
}