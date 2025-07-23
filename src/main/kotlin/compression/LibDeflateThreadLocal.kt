package org.chorus_oss.chorus.compression

import cn.powernukkitx.libdeflate.CompressionType
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.utils.LibDeflator
import org.chorus_oss.chorus.utils.LibInflator
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.DataFormatException

class LibDeflateThreadLocal(private val zlibThreadLocal: ZlibThreadLocal?) : ZlibProvider {
    @Throws(IOException::class)
    override fun deflate(data: ByteArray, level: Int, raw: Boolean): ByteArray {
        val deflator = DEFLATOR.get()
        val type = if (raw) CompressionType.DEFLATE else CompressionType.ZLIB
        val buffer =
            if (deflator.getCompressBound(data.size.toLong(), type) < 8192) BUFFER.get() else ByteArray(data.size)
        val compressedSize = deflator.compress(data, buffer, type)
        if (compressedSize <= 0) {
            return zlibThreadLocal!!.deflate(data, level, raw)
        }
        val output = ByteArray(compressedSize)
        System.arraycopy(buffer, 0, output, 0, compressedSize)
        return output
    }

    // decompress
    @Throws(IOException::class)
    override fun inflate(data: ByteArray, maxSize: Int, raw: Boolean): ByteArray {
        val type = if (raw) CompressionType.DEFLATE else CompressionType.ZLIB
        val inflator = INFLATOR.get()
        try {
            if (maxSize < 8192) {
                val buffer = BUFFER.get()
                val result = inflator.decompressUnknownSize(data, 0, data.size, buffer, 0, buffer.size, type)
                if (result == -1L) {
                    return inflateD(data, maxSize, type)
                } else if (maxSize in 1..<result) {
                    throw IOException("Inflated data exceeds maximum size")
                }
                val output = ByteArray(result.toInt())
                System.arraycopy(buffer, 0, output, 0, output.size)
                return output
            } else {
                return inflateD(data, maxSize, type)
            }
        } catch (e: DataFormatException) {
            throw IOException("Unable to inflate zlib stream", e)
        }
    }

    @Throws(IOException::class)
    fun inflateD(data: ByteArray, maxSize: Int, type: CompressionType): ByteArray {
        val inflator = INFLATOR.get()
        var directBuffer: ByteBuffer? = null
        try {
            directBuffer = DIRECT_BUFFER.get()
            if (directBuffer == null || directBuffer.capacity() == 0 || data.size > directBuffer.capacity()) {
                return inflate0(data, maxSize, type)
            }
            val result: Long
            try {
                result = inflator.decompressUnknownSize(ByteBuffer.wrap(data), directBuffer, type)
                if (result == -1L) {
                    return inflate0(data, maxSize, type)
                } else if (maxSize in 1..<result) {
                    throw IOException("Inflated data exceeds maximum size")
                }
            } catch (ignore: IllegalArgumentException) {
                return inflate0(data, maxSize, type)
            }
            val output = ByteArray(result.toInt())
            directBuffer[0, output, 0, output.size]
            return output
        } catch (e: DataFormatException) {
            throw IOException("Unable to inflate zlib stream", e)
        } finally {
            directBuffer?.clear()
        }
    }

    //Fallback
    @Throws(IOException::class)
    fun inflate0(data: ByteArray, maxSize: Int, type: CompressionType): ByteArray {
        return zlibThreadLocal!!.inflate(data, maxSize, type == CompressionType.DEFLATE)
    }

    companion object {
        private val INFLATOR: ThreadLocal<LibInflator> = ThreadLocal.withInitial {
            LibInflator()
        }
        private val DEFLATOR: ThreadLocal<LibDeflator> = ThreadLocal.withInitial {
            LibDeflator()
        }
        private val BUFFER: ThreadLocal<ByteArray> = ThreadLocal.withInitial {
            ByteArray(
                8192
            )
        }

        private val DIRECT_BUFFER: ThreadLocal<ByteBuffer> = ThreadLocal.withInitial<ByteBuffer> {
            val maximumSizePerChunk: Int = Server.instance.settings.networkSettings.compressionBufferSize
            if (maximumSizePerChunk < 8192 || maximumSizePerChunk > 1024 * 1024 * 16) {
                return@withInitial null
            } else {
                return@withInitial ByteBuffer.allocateDirect(maximumSizePerChunk)
                    .order(ByteOrder.nativeOrder())
            }
        }
    }
}
