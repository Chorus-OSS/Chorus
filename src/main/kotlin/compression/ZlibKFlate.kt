package org.chorus_oss.chorus.compression

import org.chorus_oss.kflate.*

class ZlibKFlate : ZlibProvider {
    override fun deflate(data: ByteArray, level: Int, raw: Boolean): ByteArray {
        val compressor: Compressor = when (raw) {
            true -> DeflateCompressor(level)
            false -> ZlibCompressor(level)
        }
        return compressor.compress(data)
    }

    override fun inflate(data: ByteArray, maxSize: Int, raw: Boolean): ByteArray {
        val decompressor: Decompressor = when(raw) {
            true -> DeflateDecompressor()
            false -> ZlibDecompressor()
        }
        return decompressor.decompress(data)
    }
}
