package org.chorus_oss.chorus.nbt.stream

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.VarInt
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean


class NBTOutputStream @JvmOverloads constructor(
    stream: OutputStream?,
    val endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
    val isNetwork: Boolean = false
) :
    DataOutput, AutoCloseable {
    private val stream = if (stream is DataOutputStream) stream else DataOutputStream(stream)
    private val closed = AtomicBoolean(false)

    @Throws(IOException::class)
    override fun write(bytes: ByteArray) {
        stream.write(bytes)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        stream.write(b, off, len)
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        stream.write(b)
    }

    @Throws(IOException::class)
    override fun writeBoolean(v: Boolean) {
        stream.writeBoolean(v)
    }

    @Throws(IOException::class)
    override fun writeByte(v: Int) {
        stream.writeByte(v)
    }

    @Throws(IOException::class)
    override fun writeShort(v: Int) {
        var v = v
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            v = Integer.reverseBytes(v) shr 16
        }
        stream.writeShort(v)
    }

    @Throws(IOException::class)
    override fun writeChar(v: Int) {
        var v = v
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            v = Character.reverseBytes(v.toChar()).code
        }
        stream.writeChar(v)
    }

    @Throws(IOException::class)
    override fun writeInt(v: Int) {
        var v = v
        if (isNetwork) {
            VarInt.writeVarInt(this.stream, v)
        } else {
            if (endianness == ByteOrder.LITTLE_ENDIAN) {
                v = Integer.reverseBytes(v)
            }
            stream.writeInt(v)
        }
    }

    @Throws(IOException::class)
    override fun writeLong(v: Long) {
        var v = v
        if (isNetwork) {
            VarInt.writeVarLong(this.stream, v)
        } else {
            if (endianness == ByteOrder.LITTLE_ENDIAN) {
                v = java.lang.Long.reverseBytes(v)
            }
            stream.writeLong(v)
        }
    }

    @Throws(IOException::class)
    override fun writeFloat(v: Float) {
        var i = java.lang.Float.floatToIntBits(v)
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            i = Integer.reverseBytes(i)
        }
        stream.writeInt(i)
    }

    @Throws(IOException::class)
    override fun writeDouble(v: Double) {
        var l = java.lang.Double.doubleToLongBits(v)
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            l = java.lang.Long.reverseBytes(l)
        }
        stream.writeLong(l)
    }

    @Throws(IOException::class)
    override fun writeBytes(s: String) {
        stream.writeBytes(s)
    }

    @Throws(IOException::class)
    override fun writeChars(s: String) {
        stream.writeChars(s)
    }

    @Throws(IOException::class)
    override fun writeUTF(s: String) {
        val bytes = s.toByteArray(StandardCharsets.UTF_8)
        if (isNetwork) {
            VarInt.writeUnsignedVarInt(stream, bytes.size.toLong())
        } else {
            this.writeShort(bytes.size)
        }
        stream.write(bytes)
    }

    @Throws(IOException::class)
    override fun close() {
        closed.set(true)
        stream.close()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeTag(tag: Tag<*>, maxDepth: Int = 16) {
        check(!closed.get()) { "closed" }
        val type = tag.id.toInt()
        this.writeByte(type)
        this.writeUTF("")
        this.serialize(tag, type, maxDepth)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeValue(tag: Tag<*>, maxDepth: Int = 16) {
        check(!closed.get()) { "closed" }
        this.serialize(tag, tag.id.toInt(), maxDepth)
    }

    @Throws(IOException::class)
    private fun serialize(tag: Tag<*>, type: Int, maxDepth: Int) {
        require(maxDepth >= 0) { "Reached depth limit" }
        when (type.toByte()) {
            Tag.TAG_BYTE -> this.writeByte(tag.parseValue() as Int)
            Tag.TAG_SHORT -> this.writeShort((tag.parseValue() as Short).toInt())
            Tag.TAG_INT -> this.writeInt(tag.parseValue() as Int)
            Tag.TAG_LONG -> this.writeLong(tag.parseValue() as Long)
            Tag.TAG_FLOAT -> this.writeFloat(tag.parseValue() as Float)
            Tag.TAG_DOUBLE -> this.writeDouble(tag.parseValue() as Double)
            Tag.TAG_BYTE_ARRAY -> {
                val byteArray = tag.parseValue() as ByteArray
                this.writeInt(byteArray.size)
                this.write(byteArray)
            }

            Tag.Companion.TAG_STRING -> {
                val string = tag.parseValue() as String
                this.writeUTF(string)
            }

            Tag.Companion.TAG_COMPOUND -> {
                val map = tag as CompoundTag
                for ((key, value) in map.tags) {
                    this.writeByte(value.id.toInt())
                    this.writeUTF(key)
                    this.serialize(value, value.id.toInt(), maxDepth - 1)
                }
                this.writeByte(0) // End tag
            }

            Tag.Companion.TAG_LIST -> {
                val list = tag as ListTag<out Tag<*>>
                this.writeByte(list.type.toInt())
                this.writeInt(list.size())
                for (t in list.all) {
                    this.serialize(t, list.type.toInt(), maxDepth - 1)
                }
            }

            else -> Unit
        }
    }
}
