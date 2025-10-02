package org.chorus_oss.chorus.experimental.network.connection.compression

import kotlinx.io.bytestring.ByteString

class NOOPCompressor : Compressor {
    override fun compress(data: ByteString): ByteString = data

    override fun decompress(data: ByteString): ByteString = data
}