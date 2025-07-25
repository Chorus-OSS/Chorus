package org.chorus_oss.chorus.utils

import io.netty.buffer.ByteBuf

import java.math.BigInteger

object ByteBufVarInt {
    fun writeInt(buffer: ByteBuf, value: Int) {
        encode(buffer, ((value shl 1) xor (value shr 31)).toLong() and 0xFFFFFFFFL)
    }

    fun readInt(buffer: ByteBuf): Int {
        val n = decode(buffer, 32).toInt()
        return (n ushr 1) xor -(n and 1)
    }

    fun writeUnsignedInt(buffer: ByteBuf, value: Int) {
        encode(buffer, value.toLong() and 0xFFFFFFFFL)
    }

    fun readUnsignedInt(buffer: ByteBuf): Int {
        return decode(buffer, 32).toInt()
    }

    fun writeLong(buffer: ByteBuf, value: Long) {
        encode(buffer, (value shl 1) xor (value shr 63))
    }

    fun readLong(buffer: ByteBuf): Long {
        val n = decode(buffer, 64)
        return (n ushr 1) xor -(n and 1L)
    }

    fun writeUnsignedLong(buffer: ByteBuf, value: Long) {
        encode(buffer, value)
    }

    fun readUnsignedLong(buffer: ByteBuf): Long {
        return decode(buffer, 64)
    }

    // Based off of Andrew Steinborn's blog post:
    // https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
    private fun encode(buf: ByteBuf, value: Long) {
        // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
        // that the server will write, to improve inlining.
        if ((value and 0x7FL.inv()) == 0L) {
            buf.writeByte(value.toByte().toInt())
        } else if ((value and 0x3FFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 8 or
                    (value ushr 7)).toInt()
            buf.writeShort(w)
        } else {
            encodeFull(buf, value)
        }
    }

    private fun encodeFull(buf: ByteBuf, value: Long) {
        if ((value and 0x7FL.inv()) == 0L) {
            buf.writeByte(value.toByte().toInt())
        } else if ((value and 0x3FFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 8 or
                    (value ushr 7)).toInt()
            buf.writeShort(w)
        } else if ((value and 0x1FFFFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 16 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 8) or
                    (value ushr 14)).toInt()
            buf.writeMedium(w)
        } else if ((value and 0xFFFFFFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 24 or
                    (((value ushr 7) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 8) or
                    (value ushr 21)).toInt()
            buf.writeInt(w)
        } else if ((value and 0x7FFFFFFFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 24 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 8) or
                    ((value ushr 21) and 0x7FL or 0x80L)).toInt()
            buf.writeInt(w)
            buf.writeByte((value ushr 28).toInt())
        } else if ((value and 0x3FFFFFFFFFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 24 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 8) or
                    ((value ushr 21) and 0x7FL or 0x80L)).toInt()
            val w2 = (((value ushr 28) and 0x7FL or 0x80L) shl 8 or
                    (value ushr 35)).toInt()
            buf.writeInt(w)
            buf.writeShort(w2)
        } else if ((value and 0x1FFFFFFFFFFFFL.inv()) == 0L) {
            val w = ((value and 0x7FL or 0x80L) shl 24 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 8) or
                    ((value ushr 21) and 0x7FL or 0x80L)).toInt()
            val w2 = ((((value ushr 28) and 0x7FL or 0x80L) shl 16 or
                    (((value ushr 35) and 0x7FL or 0x80L) shl 8)) or
                    (value ushr 42)).toInt()
            buf.writeInt(w)
            buf.writeMedium(w2)
        } else if ((value and 0xFFFFFFFFFFFFFFL.inv()) == 0L) {
            val w = (value and 0x7FL or 0x80L) shl 56 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 48) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 40) or (
                    ((value ushr 21) and 0x7FL or 0x80L) shl 32) or (
                    ((value ushr 28) and 0x7FL or 0x80L) shl 24) or (
                    ((value ushr 35) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 42) and 0x7FL or 0x80L) shl 8) or
                    (value ushr 49)
            buf.writeLong(w)
        } else if ((value and 0x7FFFFFFFFFFFFFFFL.inv()) == 0L) {
            val w = (value and 0x7FL or 0x80L) shl 56 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 48) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 40) or (
                    ((value ushr 21) and 0x7FL or 0x80L) shl 32) or (
                    ((value ushr 28) and 0x7FL or 0x80L) shl 24) or (
                    ((value ushr 35) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 42) and 0x7FL or 0x80L) shl 8) or
                    ((value ushr 49) and 0x7FL or 0x80L)
            buf.writeLong(w)
            buf.writeByte((value ushr 56).toByte().toInt())
        } else {
            val w = (value and 0x7FL or 0x80L) shl 56 or (
                    ((value ushr 7) and 0x7FL or 0x80L) shl 48) or (
                    ((value ushr 14) and 0x7FL or 0x80L) shl 40) or (
                    ((value ushr 21) and 0x7FL or 0x80L) shl 32) or (
                    ((value ushr 28) and 0x7FL or 0x80L) shl 24) or (
                    ((value ushr 35) and 0x7FL or 0x80L) shl 16) or (
                    ((value ushr 42) and 0x7FL or 0x80L) shl 8) or
                    ((value ushr 49) and 0x7FL or 0x80L)
            val w2 = ((value ushr 56) and 0x7FL or 0x80L) shl 8 or
                    (value ushr 63)
            buf.writeLong(w)
            buf.writeShort(w2.toInt())
        }
    }

    private fun decode(buf: ByteBuf, maxBits: Int): Long {
        var result: Long = 0
        var shift = 0
        while (shift < maxBits) {
            val b = buf.readByte()
            result = result or ((b.toLong() and 0x7FL) shl shift)
            if ((b.toInt() and 0x80) == 0) {
                return result
            }
            shift += 7
        }
        throw ArithmeticException("VarInt was too large")
    }

    private val BIG_INTEGER_7F: BigInteger = BigInteger.valueOf(0x7f)
    private val BIG_INTEGER_80: BigInteger = BigInteger.valueOf(0x80)

    fun writeUnsignedBigVarInt(buffer: ByteBuf, value: BigInteger) {
        var value1 = value
        while (true) {
            val bits = value1.and(BIG_INTEGER_7F)
            value1 = value1.shiftRight(7)
            if (value1.compareTo(BigInteger.ZERO) == 0) {
                buffer.writeByte(bits.toInt())
                return
            }
            buffer.writeByte(bits.or(BIG_INTEGER_80).toInt())
        }
    }

    fun readUnsignedBigVarInt(buffer: ByteBuf, maxBits: Int): BigInteger {
        var value = BigInteger.ZERO
        var shift = 0
        while (true) {
            if (shift >= maxBits) {
                throw ArithmeticException("VarInt was too large")
            }

            val b = buffer.readByte()
            value = value.or(BigInteger.valueOf((b.toInt() and 0x7F).toLong()).shiftLeft(shift))
            if ((b.toInt() and 0x80) == 0) {
                return value
            }
            shift += 7
        }
    }
}
