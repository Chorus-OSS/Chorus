package org.chorus_oss.chorus.utils

import java.util.*

object Binary {

    @JvmStatic
    fun writeUUID(uuid: UUID): ByteArray {
        return appendBytes(writeLLong(uuid.mostSignificantBits), writeLLong(uuid.leastSignificantBits))
    }

    fun readInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xff) shl 24) +
                ((bytes[1].toInt() and 0xff) shl 16) +
                ((bytes[2].toInt() and 0xff) shl 8) +
                (bytes[3].toInt() and 0xff)
    }

    fun writeInt(i: Int): ByteArray {
        return byteArrayOf(
            ((i ushr 24) and 0xFF).toByte(),
            ((i ushr 16) and 0xFF).toByte(),
            ((i ushr 8) and 0xFF).toByte(),
            (i and 0xFF).toByte()
        )
    }

    fun writeLLong(l: Long): ByteArray {
        return byteArrayOf(
            (l).toByte(),
            (l ushr 8).toByte(),
            (l ushr 16).toByte(),
            (l ushr 24).toByte(),
            (l ushr 32).toByte(),
            (l ushr 40).toByte(),
            (l ushr 48).toByte(),
            (l ushr 56).toByte(),
        )
    }

    fun bytesToHexString(src: ByteArray?): String? {
        return bytesToHexString(src, false)
    }

    fun bytesToHexString(src: ByteArray?, blank: Boolean): String? {
        val stringBuilder = StringBuilder()
        if (src == null || src.isEmpty()) {
            return null
        }

        for (b in src) {
            if (stringBuilder.isNotEmpty() && blank) {
                stringBuilder.append(" ")
            }
            val v = b.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString().uppercase()
    }

    fun appendBytes(bytes1: ByteArray, vararg bytes2: ByteArray): ByteArray {
        var length = bytes1.size
        for (bytes in bytes2) {
            length += bytes.size
        }

        val appendedBytes = ByteArray(length)
        System.arraycopy(bytes1, 0, appendedBytes, 0, bytes1.size)
        var index = bytes1.size

        for (b in bytes2) {
            System.arraycopy(b, 0, appendedBytes, index, b.size)
            index += b.size
        }
        return appendedBytes
    }

}
