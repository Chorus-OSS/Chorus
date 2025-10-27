package org.chorus_oss.chorus.experimental.network.connection.compression

class NOOPCompressor : Compressor {
    override fun compress(data: ByteArray): ByteArray = data

    override fun decompress(data: ByteArray): ByteArray = data
}