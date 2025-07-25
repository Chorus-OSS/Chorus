package org.chorus_oss.chorus.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

class ZlibThreadLocal : ZlibProvider {
    @Throws(IOException::class)
    override fun deflate(data: ByteArray, level: Int, raw: Boolean): ByteArray {
        val deflater = if (raw) DEFLATER_RAW.get() else DEFLATER.get()
        deflater.reset()
        deflater.setLevel(level)
        deflater.setInput(data)
        deflater.finish()
        val buffer = BUFFER.get()
        val bos = ByteArrayOutputStream(1024)
        try {
            while (!deflater.finished()) {
                val i = deflater.deflate(buffer)
                bos.write(buffer, 0, i)
            }
        } finally {
            deflater.reset()
        }
        return bos.toByteArray()
    }

    @Throws(IOException::class)
    override fun inflate(data: ByteArray, maxSize: Int, raw: Boolean): ByteArray {
        val inflater = if (raw) INFLATER_RAW.get() else INFLATER.get()
        inflater.reset()
        inflater.setInput(data)

        val buffer = BUFFER.get()
        val bos = ByteArrayOutputStream(1024)

        try {
            var length = 0
            while (!inflater.finished()) {
                val i = inflater.inflate(buffer)
                length += i
                if (maxSize in 1..<length) {
                    throw IOException("Inflated data exceeds maximum size")
                }
                bos.write(buffer, 0, i)
            }
            return bos.toByteArray()
        } catch (e: DataFormatException) {
            throw IOException("Unable to inflate zlib stream", e)
        } finally {
            inflater.reset()
        }
    }

    companion object {
        private val INFLATER: ThreadLocal<Inflater> = ThreadLocal.withInitial { Inflater() }
        private val DEFLATER: ThreadLocal<Deflater> = ThreadLocal.withInitial { Deflater() }
        private val INFLATER_RAW: ThreadLocal<Inflater> = ThreadLocal.withInitial { Inflater(true) }
        private val DEFLATER_RAW: ThreadLocal<Deflater> = ThreadLocal.withInitial { Deflater(-1, true) }
        private val BUFFER: ThreadLocal<ByteArray> = ThreadLocal.withInitial { ByteArray(8192) }
    }
}
