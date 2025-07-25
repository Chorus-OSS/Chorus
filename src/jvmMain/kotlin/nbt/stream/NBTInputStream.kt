package org.chorus_oss.chorus.nbt.stream

import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.utils.VarInt
import java.io.DataInput
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean


class NBTInputStream @JvmOverloads constructor(
    stream: InputStream,
    val endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
    val isNetwork: Boolean = false
) :
    DataInput, AutoCloseable {
    private val stream = if (stream is DataInputStream) stream else DataInputStream(stream)
    private val closed = AtomicBoolean(false)

    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        stream.readFully(b)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray, off: Int, len: Int) {
        stream.readFully(b, off, len)
    }

    @Throws(IOException::class)
    override fun skipBytes(n: Int): Int {
        return stream.skipBytes(n)
    }

    @Throws(IOException::class)
    override fun readBoolean(): Boolean {
        return stream.readBoolean()
    }

    @Throws(IOException::class)
    override fun readByte(): Byte {
        return stream.readByte()
    }

    @Throws(IOException::class)
    override fun readUnsignedByte(): Int {
        return stream.readUnsignedByte()
    }

    @Throws(IOException::class)
    override fun readShort(): Short {
        var s = stream.readShort()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            s = java.lang.Short.reverseBytes(s)
        }
        return s
    }

    @Throws(IOException::class)
    override fun readUnsignedShort(): Int {
        var s = stream.readUnsignedShort()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            s = Integer.reverseBytes(s) shr 16
        }
        return s
    }

    @Throws(IOException::class)
    override fun readChar(): Char {
        var c = stream.readChar()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            c = Character.reverseBytes(c)
        }
        return c
    }

    @Throws(IOException::class)
    override fun readInt(): Int {
        if (isNetwork) {
            return VarInt.readVarInt(this.stream)
        }
        var i = stream.readInt()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            i = Integer.reverseBytes(i)
        }
        return i
    }

    @Throws(IOException::class)
    override fun readLong(): Long {
        if (isNetwork) {
            return VarInt.readVarLong(this.stream)
        }
        var l = stream.readLong()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            l = java.lang.Long.reverseBytes(l)
        }
        return l
    }

    @Throws(IOException::class)
    override fun readFloat(): Float {
        var i = stream.readInt()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            i = Integer.reverseBytes(i)
        }
        return java.lang.Float.intBitsToFloat(i)
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        var l = stream.readLong()
        if (endianness == ByteOrder.LITTLE_ENDIAN) {
            l = java.lang.Long.reverseBytes(l)
        }
        return java.lang.Double.longBitsToDouble(l)
    }

    @Deprecated("")
    @Throws(IOException::class)
    override fun readLine(): String {
        return stream.readLine()
    }

    @Throws(IOException::class)
    override fun readUTF(): String {
        val length = if (isNetwork) VarInt.readUnsignedVarInt(stream).toInt() else this.readUnsignedShort()
        val bytes = ByteArray(length)
        stream.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    fun available(): Int {
        return stream.available()
    }

    @Throws(IOException::class)
    override fun close() {
        closed.set(true)
        stream.close()
    }

    @Throws(IOException::class)
    fun readTag(): Any {
        return this.readTag(16)
    }

    @Throws(IOException::class)
    fun readTag(maxDepth: Int): Any {
        check(!closed.get()) { "Trying to read from a closed reader!" }
        val typeId = this.readUnsignedByte()
        this.readUTF()
        return this.deserialize(typeId, maxDepth)
    }

    @Throws(IOException::class)
    fun <T : Tag<*>> readValue(type: Int): T {
        return this.readValue(type, 16)
    }

    @Throws(IOException::class)
    fun <T : Tag<*>> readValue(type: Int, maxDepth: Int): T {
        check(!closed.get()) { "Trying to read from a closed reader!" }
        return deserialize(type, maxDepth) as T
    }

    @Throws(IOException::class)
    private fun deserialize(type: Int, maxDepth: Int): Tag<*> {
        require(maxDepth >= 0) { "NBT compound is too deeply nested" }
        val arraySize: Int
        when (type.toByte()) {
            Tag.Companion.TAG_END -> return EndTag()
            Tag.Companion.TAG_BYTE -> return ByteTag(readByte().toInt())
            Tag.Companion.TAG_SHORT -> return ShortTag(readShort().toInt())
            Tag.Companion.TAG_INT -> return IntTag(readInt())
            Tag.Companion.TAG_LONG -> return LongTag(readLong())
            Tag.Companion.TAG_FLOAT -> return FloatTag(readFloat())
            Tag.Companion.TAG_DOUBLE -> return DoubleTag(readDouble())
            Tag.Companion.TAG_BYTE_ARRAY -> {
                arraySize = this.readInt()
                val bytes = ByteArray(arraySize)
                this.readFully(bytes)
                return ByteArrayTag(bytes)
            }

            Tag.Companion.TAG_STRING -> return StringTag(this.readUTF())
            Tag.Companion.TAG_COMPOUND -> {
                val map = LinkedHashMap<String, Tag<*>>()
                var nbtType: Int
                while ((readUnsignedByte().also { nbtType = it }) != Tag.Companion.TAG_END.toInt()) {
                    val name = this.readUTF()
                    map[name] = deserialize(nbtType, maxDepth - 1)
                }
                return CompoundTag(map)
            }

            Tag.Companion.TAG_LIST -> {
                val typeId = this.readUnsignedByte()
                val listLength = this.readInt()
                val list: MutableList<Tag<*>> = ArrayList(listLength)

                var i = 0
                while (i < listLength) {
                    list.add(this.deserialize(typeId, maxDepth - 1))
                    ++i
                }
                return ListTag(typeId, list)
            }

            Tag.Companion.TAG_INT_ARRAY -> {
                arraySize = this.readInt()
                val ints = IntArray(arraySize)

                var i = 0
                while (i < arraySize) {
                    ints[i] = this.readInt()
                    ++i
                }
                return IntArrayTag(ints)
            }

            else -> throw IllegalArgumentException("Unknown type $type")
        }
    }
}
