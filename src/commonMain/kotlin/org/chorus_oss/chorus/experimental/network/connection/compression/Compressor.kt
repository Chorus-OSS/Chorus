package org.chorus_oss.chorus.experimental.network.connection.compression

import kotlinx.io.bytestring.ByteString

sealed interface Compressor {
    fun compress(data: ByteString): ByteString

    fun decompress(data: ByteString): ByteString
}