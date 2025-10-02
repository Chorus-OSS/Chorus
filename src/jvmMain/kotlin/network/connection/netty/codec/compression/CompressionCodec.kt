package org.chorus_oss.chorus.network.connection.netty.codec.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm

class CompressionCodec(val strategy: CompressionStrategy, private val prefixed: Boolean) :
    MessageToMessageCodec<BedrockBatchWrapper, BedrockBatchWrapper>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: BedrockBatchWrapper, out: MutableList<Any>) {
        check(!(msg.compressed == null && msg.uncompressed == null)) { "Batch was not encoded before" }

        if (msg.compressed != null && !msg.modified) {
            out.add(msg.retain())
            return
        }

        val compression = strategy.getCompression(msg)
        check(!(!this.prefixed && strategy.defaultCompression.algorithm !== compression.algorithm)) { "Non-default compression algorithm used without prefixing" }

        val compressed = compression.encode(ctx, msg.uncompressed!!)
        try {
            val outBuf: ByteBuf
            if (this.prefixed) {
                // Do not use a composite buffer as encryption does not like it
                outBuf = ctx.alloc().ioBuffer(1 + compressed!!.readableBytes())
                outBuf.writeByte(getCompressionHeader(compression.algorithm).toInt())
                outBuf.writeBytes(compressed)
            } else {
                outBuf = compressed!!.retain()
            }

            msg.setCompressed(outBuf)
        } finally {
            compressed!!.release()
        }

        out.add(msg.retain())
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: BedrockBatchWrapper, out: MutableList<Any>) {
        val compressed = msg.compressed!!.slice()
        check(
            compressed.capacity() <= Server.instance.settings.networkSettings.maxDecompressSize
        ) { "Compressed data size is too big: ${compressed.capacity()}" }

        val compression: BatchCompression?
        if (this.prefixed) {
            val compressionHeader = compressed.readByte()
            val algorithm = this.getCompressionAlgorithm(compressionHeader)
            compression = strategy.getCompression(algorithm)
        } else {
            compression = strategy.defaultCompression
        }

        msg.setUncompressed(compression!!.decode(ctx, compressed.slice()))
        out.add(msg.retain())
    }

    protected fun getCompressionHeader(algorithm: CompressionAlgorithm): Byte {
        return when (algorithm) {
            PacketCompressionAlgorithm.NONE -> 0xff.toByte()
            PacketCompressionAlgorithm.ZLIB -> 0x00
            PacketCompressionAlgorithm.SNAPPY -> 0x01
            else -> throw IllegalArgumentException("Unknown compression algorithm $algorithm")
        }
    }

    protected fun getCompressionAlgorithm(header: Byte): CompressionAlgorithm {
        return when (header) {
            0x00.toByte() -> PacketCompressionAlgorithm.ZLIB
            0x01.toByte() -> PacketCompressionAlgorithm.SNAPPY
            0xff.toByte() -> PacketCompressionAlgorithm.NONE
            else -> throw IllegalArgumentException("Unknown compression algorithm $header")
        }
    }

    override fun toString(): String {
        return strategy.toString()
    }

    companion object {
        const val NAME: String = "compression-codec"
    }
}
