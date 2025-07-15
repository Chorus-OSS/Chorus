package org.chorus_oss.chorus.network.connection.netty.codec.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import kotlinx.io.Buffer
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.connection.netty.BedrockPacketWrapper
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.PacketRegistry

abstract class BedrockPacketCodec : MessageToMessageCodec<ByteBuf, BedrockPacketWrapper>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: BedrockPacketWrapper, out: MutableList<Any>) {
        if (msg.packetBuffer != null) {
            // We have a pre-encoded packet buffer, just use that.
            out.add(msg.retain())
        } else {
            val buf = ctx.alloc().buffer(128)
            try {
                val packet = msg.packet
                msg.packetId = packet!!.pid()
                encodeHeader(buf, msg)
                packet.encode(HandleByteBuf.of(buf))
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

            val packetDecoder = Registries.PACKET_DECODER[wrapper.packetId]
            if (packetDecoder != null) {
                wrapper.packet = packetDecoder.decode(HandleByteBuf.of(msg))
            } else {
                wrapper.packet = MigrationPacket(
                    codec.deserialize(Buffer().apply {
                        val bytes = ByteArray(msg.readableBytes())
                        msg.readBytes(bytes)
                        write(bytes)
                    }) as Packet
                )
            }
            out.add(wrapper.retain())
        } catch (t: Throwable) {
            log.info("Failed to decode PacketID: ${wrapper.packetId}", t)
            throw t
        } finally {
            wrapper.release()
        }
    }

    abstract fun encodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper)

    abstract fun decodeHeader(buf: ByteBuf, msg: BedrockPacketWrapper)

    companion object : Loggable {
        const val NAME: String = "bedrock-packet-codec"
    }
}
