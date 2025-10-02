package org.chorus_oss.chorus.experimental.network.connection.compression

import kotlinx.io.bytestring.ByteString
import org.chorus_oss.snappy.SnappyCompressor
import org.chorus_oss.snappy.SnappyDecompressor

class SnappyCompressor : Compressor {
    private val compressor = SnappyCompressor()
    private val decompressor = SnappyDecompressor()

    override fun compress(data: ByteString): ByteString = compressor.compress(data)

    override fun decompress(data: ByteString): ByteString = decompressor.decompress(data)
}