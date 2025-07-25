package org.chorus_oss.chorus.network.connection.netty.codec.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm


class NoopCompression : BatchCompression {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        return msg.retainedSlice()
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        return msg.retainedSlice()
    }

    override val algorithm: CompressionAlgorithm
        get() = PacketCompressionAlgorithm.NONE

    override val level: Int = -1
}
