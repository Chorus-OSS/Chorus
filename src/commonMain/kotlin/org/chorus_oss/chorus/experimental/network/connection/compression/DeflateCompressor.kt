package org.chorus_oss.chorus.experimental.network.connection.compression

import org.chorus_oss.kflate.DeflateCompressor
import org.chorus_oss.kflate.DeflateDecompressor

class DeflateCompressor(level: Int = 7) : Compressor {
    private val compressor = DeflateCompressor(level)
    private val decompressor = DeflateDecompressor()

    override fun compress(data: ByteArray): ByteArray = compressor.compress(data)

    override fun decompress(data: ByteArray): ByteArray = decompressor.decompress(data)
}