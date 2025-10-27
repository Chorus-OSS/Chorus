package org.chorus_oss.chorus.experimental.network.connection.compression

import org.chorus_oss.snappy.SnappyCompressor
import org.chorus_oss.snappy.SnappyDecompressor

class SnappyCompressor : Compressor {
    private val compressor = SnappyCompressor()
    private val decompressor = SnappyDecompressor()

    override fun compress(data: ByteArray): ByteArray = compressor.compress(data)

    override fun decompress(data: ByteArray): ByteArray = decompressor.decompress(data)
}