package org.chorus_oss.chorus.experimental.network.connection.compression

sealed interface Compressor {
    fun compress(data: ByteArray): ByteArray

    fun decompress(data: ByteArray): ByteArray
}