package org.chorus_oss.chorus.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object VarInt {
    private fun encodeZigZag32(v: Int): Long {
        return ((v shl 1) xor (v shr 31)).toLong() and 0xFFFFFFFFL
    }

    private fun decodeZigZag32(v: Long): Int {
        return (v shr 1).toInt() xor -(v and 1L).toInt()
    }

    private fun encodeZigZag64(v: Long): Long {
        return (v shl 1) xor (v shr 63)
    }

    private fun decodeZigZag64(v: Long): Long {
        return (v ushr 1) xor -(v and 1L)
    }

    @Throws(IOException::class)
    private fun read(stream: InputStream, maxSize: Int): Long {
        var value: Long = 0
        var size = 0
        var b: Int
        while (((stream.read().also { b = it }) and 0x80) == 0x80) {
            value = value or ((b and 0x7F).toLong() shl (size++ * 7))
            require(size < maxSize) { "VarLong too big" }
        }

        return value or ((b and 0x7F).toLong() shl (size * 7))
    }

    /**
     * @param stream InputStream
     * @return Signed int
     */
    @Throws(IOException::class)
    fun readVarInt(stream: InputStream): Int {
        return decodeZigZag32(readUnsignedVarInt(stream))
    }

    /**
     * @param stream InputStream
     * @return Unsigned int
     */
    @Throws(IOException::class)
    fun readUnsignedVarInt(stream: InputStream): Long {
        return read(stream, 5)
    }

    /**
     * @param stream InputStream
     * @return Signed long
     */
    @Throws(IOException::class)
    fun readVarLong(stream: InputStream): Long {
        return decodeZigZag64(readUnsignedVarLong(stream))
    }

    /**
     * @param stream InputStream
     * @return Unsigned long
     */
    @Throws(IOException::class)
    fun readUnsignedVarLong(stream: InputStream): Long {
        return read(stream, 10)
    }

    @Throws(IOException::class)
    private fun write(stream: OutputStream, value: Long) {
        var value1 = value
        do {
            var temp = (value1 and 127L).toByte()
            value1 = value1 ushr 7
            if (value1 != 0L) {
                temp = (temp.toInt() or 128).toByte()
            }
            stream.write(temp.toInt())
        } while (value1 != 0L)
    }

    /**
     * @param stream OutputStream
     * @param value  Signed int
     */
    @Throws(IOException::class)
    fun writeVarInt(stream: OutputStream, value: Int) {
        writeUnsignedVarInt(stream, encodeZigZag32(value))
    }

    /**
     * @param stream OutputStream
     * @param value  Unsigned int
     */
    @Throws(IOException::class)
    fun writeUnsignedVarInt(stream: OutputStream, value: Long) {
        write(stream, value)
    }

    /**
     * @param stream OutputStream
     * @param value  Signed long
     */
    @Throws(IOException::class)
    fun writeVarLong(stream: OutputStream, value: Long) {
        writeUnsignedVarLong(stream, encodeZigZag64(value))
    }

    /**
     * @param stream OutputStream
     * @param value  Unsigned long
     */
    @Throws(IOException::class)
    fun writeUnsignedVarLong(stream: OutputStream, value: Long) {
        write(stream, value)
    }
}