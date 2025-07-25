package org.chorus_oss.chorus.network.connection.netty.codec.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.chorus_oss.chorus.compression.CompressionProvider
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm


class SnappyCompression : BatchCompression {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        val readableBytes = msg.readableBytes()
        val output = ctx.alloc().ioBuffer(readableBytes)
        try {
            val data = ByteArray(readableBytes)
            msg.readBytes(data)
            val compress = CompressionProvider.SNAPPY.compress(data, 7)
            output.writeBytes(compress)
            return output.retain()
        } finally {
            output.release()
        }
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        val readableBytes = msg.readableBytes()
        val output = ctx.alloc().ioBuffer(readableBytes)
        try {
            val data = ByteArray(readableBytes)
            msg.readBytes(data)
            val compress = CompressionProvider.SNAPPY.decompress(data)
            output.writeBytes(compress)
            return output.retain()
        } finally {
            output.release()
        }
    }

    override val algorithm: CompressionAlgorithm
        get() = PacketCompressionAlgorithm.SNAPPY

    override val level: Int = -1
}
