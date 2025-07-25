package org.chorus_oss.chorus.compression

import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.xerial.snappy.Snappy
import java.io.IOException

interface CompressionProvider {
    @Throws(IOException::class)
    fun compress(data: ByteArray, level: Int): ByteArray?

    @Throws(IOException::class)
    fun decompress(compressed: ByteArray): ByteArray?

    companion object {
        fun from(algorithm: PacketCompressionAlgorithm?): CompressionProvider {
            return when (algorithm) {
                null -> NONE
                PacketCompressionAlgorithm.ZLIB -> ZLIB
                PacketCompressionAlgorithm.SNAPPY -> SNAPPY
                else -> throw UnsupportedOperationException()
            }
        }

        const val MAX_INFLATE_LEN: Int = 1024 * 1024 * 10

        val NONE: CompressionProvider = object : CompressionProvider {
            @Throws(IOException::class)
            override fun compress(data: ByteArray, level: Int): ByteArray {
                return data
            }

            @Throws(IOException::class)
            override fun decompress(compressed: ByteArray): ByteArray {
                return compressed
            }
        }

        val ZLIB: CompressionProvider = object : CompressionProvider {
            @Throws(IOException::class)
            override fun compress(data: ByteArray, level: Int): ByteArray {
                return ZlibChooser.currentProvider.deflate(data, level, false)
            }

            @Throws(IOException::class)
            override fun decompress(compressed: ByteArray): ByteArray {
                return ZlibChooser.currentProvider.inflate(compressed, MAX_INFLATE_LEN, false)
            }
        }

        val ZLIB_RAW: CompressionProvider = object : CompressionProvider {
            @Throws(IOException::class)
            override fun compress(data: ByteArray, level: Int): ByteArray {
                return ZlibChooser.currentProvider.deflate(data, level, true)
            }

            @Throws(IOException::class)
            override fun decompress(compressed: ByteArray): ByteArray {
                return ZlibChooser.currentProvider.inflate(compressed, MAX_INFLATE_LEN, true)
            }
        }

        val SNAPPY: CompressionProvider = object : CompressionProvider {
            @Throws(IOException::class)
            override fun compress(data: ByteArray, level: Int): ByteArray? {
                return Snappy.compress(data)
            }

            @Throws(IOException::class)
            override fun decompress(compressed: ByteArray): ByteArray? {
                return Snappy.uncompress(compressed)
            }
        }
    }
}