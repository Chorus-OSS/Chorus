package org.chorus_oss.chorus.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

class ZlibSingleThreadLowMem : ZlibProvider {
    @Synchronized
    @Throws(IOException::class)
    override fun deflate(data: ByteArray, level: Int, raw: Boolean): ByteArray {
        val deflater = if (raw) DEFLATER_RAW else DEFLATER
        deflater.reset()
        deflater.setLevel(level)
        deflater.setInput(data)
        deflater.finish()

        val bos = ByteArrayOutputStream(1024)
        while (!deflater.finished()) {
            val i = deflater.deflate(BUFFER)
            bos.write(BUFFER, 0, i)
        }
        return bos.toByteArray()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun inflate(data: ByteArray, maxSize: Int, raw: Boolean): ByteArray {
        val inflater = if (raw) INFLATER_RAW else INFLATER
        inflater.reset()
        inflater.setInput(data)

        val bos = ByteArrayOutputStream(1024)
        try {
            var length = 0
            while (!inflater.finished()) {
                val i = inflater.inflate(BUFFER)
                length += i
                if (maxSize in 1..<length) {
                    throw IOException("Inflated data exceeds maximum size")
                }
                bos.write(BUFFER, 0, i)
            }
            return bos.toByteArray()
        } catch (e: DataFormatException) {
            throw IOException("Unable to inflate zlib stream", e)
        }
    }

    companion object {
        private const val BUFFER_SIZE = 8192
        private val DEFLATER = Deflater(Deflater.BEST_COMPRESSION)
        private val DEFLATER_RAW = Deflater(Deflater.BEST_COMPRESSION, true)
        private val INFLATER = Inflater()
        private val INFLATER_RAW = Inflater(true)
        private val BUFFER = ByteArray(BUFFER_SIZE)
    }
}
