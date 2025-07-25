package org.chorus_oss.chorus.network.connection.netty.codec.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.chorus_oss.chorus.compression.CompressionProvider
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.chorus_oss.chorus.utils.Utils


class ZlibCompression(private val zlib: CompressionProvider) : BatchCompression {
    override val level: Int = 7

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        val outBuf = ctx.alloc().ioBuffer(msg.readableBytes())
        try {
            val bytes = Utils.convertByteBuf2Array(msg)
            val compress = zlib.compress(bytes, level)
            outBuf.writeBytes(compress)
            return outBuf.retain()
        } finally {
            outBuf.release()
        }
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf? {
        val outBuf = ctx.alloc().ioBuffer(msg.readableBytes() shl 3)
        try {
            val bytes = Utils.convertByteBuf2Array(msg)
            val decompress = zlib.decompress(bytes)
            outBuf.writeBytes(decompress)
            return outBuf.retain()
        } finally {
            outBuf.release()
        }
    }

    override val algorithm: CompressionAlgorithm
        get() = PacketCompressionAlgorithm.ZLIB
}
