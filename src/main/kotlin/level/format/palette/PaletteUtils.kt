package org.chorus_oss.chorus.level.format.palette

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.nbt.stream.NBTInputStream
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.utils.HashUtils
import org.chorus_oss.chorus.utils.SemVersion
import java.io.IOException

object PaletteUtils {
    fun fastReadBlockHash(input: NBTInputStream, byteBuf: ByteBuf): Pair<Int?, SemVersion?>? {
        try {
            byteBuf.markReaderIndex()
            val typeId = input.readUnsignedByte()
            input.skipBytes(input.readUnsignedShort()) //Skip Root tag name
            return deserialize(input, byteBuf, typeId, 16)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun deserialize(
        input: NBTInputStream,
        byteBuf: ByteBuf,
        type: Int,
        maxDepth: Int
    ): Pair<Int?, SemVersion?>? {
        require(maxDepth >= 0) { "NBT compound is too deeply nested" }
        when (type.toByte()) {
            Tag.TAG_END -> {}
            Tag.TAG_BYTE -> input.skipBytes(1)
            Tag.TAG_SHORT -> input.skipBytes(2)
            Tag.TAG_INT, Tag.TAG_FLOAT -> input.skipBytes(4)
            Tag.TAG_LONG, Tag.TAG_DOUBLE -> input.skipBytes(8)
            Tag.TAG_BYTE_ARRAY -> input.skipBytes(input.readInt())
            Tag.TAG_STRING -> input.skipBytes(input.readUnsignedShort())
            Tag.TAG_COMPOUND -> {
                var nbtType: Int
                while ((input.readUnsignedByte().also { nbtType = it }) != Tag.TAG_END.toInt()) {
                    val end = byteBuf.readerIndex()
                    val name = input.readUTF()
                    if (name == "version") {
                        val version = input.readInt()
                        byteBuf.resetReaderIndex()
                        if (version != ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION) {
                            return Pair(null, getSemVersion(version))
                        }
                        val result = ByteArray(end - byteBuf.readerIndex())
                        input.readFully(result)
                        result[result.size - 1] = 0 //because an End Tag be put when at the end serialize tag

                        input.skipBytes(input.readUnsignedShort()) //UTF
                        deserialize(input, byteBuf, nbtType, maxDepth - 1) //Value
                        input.skipBytes(1) //end tag
                        var i = HashUtils.fnv1a_32(result)
                        if (i == 147887818) i = -2 //minecraft:unknown

                        return Pair(i, null)
                    }
                    deserialize(input, byteBuf, nbtType, maxDepth - 1)
                }
            }

            Tag.TAG_LIST -> {
                val typeId = input.readUnsignedByte()
                val listLength = input.readInt()
                for (i in 0..<listLength) {
                    deserialize(input, byteBuf, typeId, maxDepth - 1)
                }
            }

            Tag.TAG_INT_ARRAY -> input.skipBytes(input.readInt() * 4)
        }
        return null
    }

    fun getSemVersion(version: Int): SemVersion {
        val major = (version shr 24) and 0xFF
        val minor = (version shr 16) and 0xFF
        val patch = (version shr 8) and 0xFF
        val revision = version and 0xFF
        return SemVersion(major, minor, patch, revision, 0)
    }
}
