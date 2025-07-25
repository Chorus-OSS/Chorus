package org.chorus_oss.chorus.network.connection.netty.codec.compression

import org.chorus_oss.chorus.compression.CompressionProvider
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm


/**
 * A simple compression strategy that uses the same compression for all packets, but
 * supports decompression using all Bedrock protocol supported algorithms.
 */
class SimpleCompressionStrategy(override val defaultCompression: BatchCompression) : CompressionStrategy {
    private val none: BatchCompression = NoopCompression()
    private var zlib: BatchCompression? = null
    private var snappy: BatchCompression? = null

    init {
        if (defaultCompression.algorithm === PacketCompressionAlgorithm.ZLIB) {
            this.zlib = defaultCompression
            this.snappy = SnappyCompression()
        } else if (defaultCompression.algorithm === PacketCompressionAlgorithm.SNAPPY) {
            this.zlib = ZlibCompression(CompressionProvider.ZLIB_RAW)
            this.snappy = defaultCompression
        } else {
            this.zlib = ZlibCompression(CompressionProvider.ZLIB_RAW)
            this.snappy = SnappyCompression()
        }
    }

    override fun getCompression(wrapper: BedrockBatchWrapper?): BatchCompression {
        return this.defaultCompression
    }

    override fun getCompression(algorithm: CompressionAlgorithm): BatchCompression? {
        if (algorithm === PacketCompressionAlgorithm.ZLIB) {
            return this.zlib
        } else if (algorithm === PacketCompressionAlgorithm.SNAPPY) {
            return this.snappy
        } else if (algorithm === PacketCompressionAlgorithm.NONE) {
            return this.none
        }
        return this.defaultCompression
    }

    override fun toString(): String {
        return "SimpleCompressionStrategy{" +
                "compression=" + defaultCompression +
                '}'
    }
}
