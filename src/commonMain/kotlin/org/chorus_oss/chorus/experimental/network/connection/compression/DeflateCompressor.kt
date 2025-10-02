package org.chorus_oss.chorus.experimental.network.connection.compression

import kotlinx.io.bytestring.ByteString
import org.chorus_oss.kflate.DeflateCompressor
import org.chorus_oss.kflate.DeflateDecompressor

class DeflateCompressor : Compressor {
    private val compressor = DeflateCompressor(7)
    private val decompressor = DeflateDecompressor()

    override fun compress(data: ByteString): ByteString = compressor.compress(data)

    override fun decompress(data: ByteString): ByteString = decompressor.decompress(data)
}