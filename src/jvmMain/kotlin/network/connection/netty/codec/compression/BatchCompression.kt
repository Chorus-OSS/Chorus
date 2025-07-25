package org.chorus_oss.chorus.network.connection.netty.codec.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm

interface BatchCompression {
    @Throws(Exception::class)
    fun encode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf?

    @Throws(Exception::class)
    fun decode(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf?

    val algorithm: CompressionAlgorithm

    val level: Int
}
