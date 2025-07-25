package org.chorus_oss.chorus.compression

import java.io.IOException

interface ZlibProvider {
    @Throws(IOException::class)
    fun deflate(data: ByteArray, level: Int, raw: Boolean): ByteArray

    @Throws(IOException::class)
    fun inflate(data: ByteArray, maxSize: Int, raw: Boolean): ByteArray
}
