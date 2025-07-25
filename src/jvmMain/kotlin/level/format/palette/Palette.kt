package org.chorus_oss.chorus.level.format.palette

import com.google.common.base.Objects
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.BlockUnknown
import org.chorus_oss.chorus.level.format.SubChunk
import org.chorus_oss.chorus.level.format.bitarray.BitArray
import org.chorus_oss.chorus.level.format.bitarray.BitArrayVersion
import org.chorus_oss.chorus.level.updater.block.BlockStateUpdaters
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.stream.NBTInputStream
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.TreeMapCompoundTag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.utils.ByteBufVarInt
import org.chorus_oss.chorus.utils.HashUtils
import org.chorus_oss.chorus.utils.Loggable
import java.io.IOException
import java.nio.ByteOrder


open class Palette<V> {
    protected val palette: MutableList<V>
    protected var bitArray: BitArray

    @JvmOverloads
    constructor(first: V, version: BitArrayVersion = BitArrayVersion.V2) {
        this.bitArray = version.createArray(SubChunk.SIZE)
        this.palette = mutableListOf()
        palette.add(first)
    }

    constructor(first: V, palette: MutableList<V>, version: BitArrayVersion) {
        this.bitArray = version.createArray(SubChunk.SIZE)
        this.palette = palette
        this.palette.add(first)
    }

    operator fun get(index: Int): V {
        val i = bitArray[index]
        return if (i >= palette.size) palette.first() else palette[i]
    }

    open operator fun set(index: Int, value: V) {
        val paletteIndex = this.paletteIndexFor(value)
        bitArray[index] = paletteIndex
    }

    /**
     * Write the Palette data to the network buffer
     *
     * @param byteBuf    the byte buf
     * @param serializer the serializer
     */
    fun writeToNetwork(byteBuf: ByteBuf, serializer: RuntimeDataSerializer<V>) {
        writeWords(byteBuf, serializer)
    }

    fun readFromNetwork(byteBuf: ByteBuf, deserializer: RuntimeDataDeserializer<V>) {
        readWords(byteBuf, readBitArrayVersion(byteBuf)!!)
        val size = bitArray.readSizeFromNetwork(byteBuf)
        for (i in 0..<size) palette.add(deserializer.deserialize(ByteBufVarInt.readInt(byteBuf)))
    }

    protected fun writeEmpty(byteBuf: ByteBuf, serializer: RuntimeDataSerializer<V>): Boolean {
        if (this.isEmpty) {
            byteBuf.writeByte(getPaletteHeader(BitArrayVersion.V0, true))
            byteBuf.writeIntLE(serializer.serialize(palette.first()))
            return true
        }
        return false
    }

    protected fun writeLast(byteBuf: ByteBuf, last: Palette<V>?): Boolean {
        if (last != null && last.palette == this.palette) {
            byteBuf.writeByte(COPY_LAST_FLAG_HEADER.toInt())
            return true
        }
        return false
    }

    protected fun writeWords(byteBuf: ByteBuf, serializer: RuntimeDataSerializer<V>) {
        byteBuf.writeByte(getPaletteHeader(bitArray.version, true))
        for (word in bitArray.words) byteBuf.writeIntLE(word)
        bitArray.writeSizeToNetwork(byteBuf, palette.size)
        for (value in palette) ByteBufVarInt.writeInt(byteBuf, serializer.serialize(value))
    }

    fun writeToStoragePersistent(byteBuf: ByteBuf, serializer: PersistentDataSerializer<V>) {
        byteBuf.writeByte(getPaletteHeader(bitArray.version, false))
        for (word in bitArray.words) byteBuf.writeIntLE(word)
        byteBuf.writeIntLE(palette.size)
        try {
            ByteBufOutputStream(byteBuf).use { bufOutputStream ->
                for (value in this.palette) {
                    value ?: continue

                    if (value is BlockState && value.identifier == BlockID.UNKNOWN) {
                        NBTIO.write(value.blockStateTag.getCompound("Block"), bufOutputStream, ByteOrder.LITTLE_ENDIAN)
                    } else {
                        NBTIO.write(serializer.serialize(value), bufOutputStream, ByteOrder.LITTLE_ENDIAN)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun readFromStoragePersistent(byteBuf: ByteBuf, deserializer: RuntimeDataDeserializer<V>) {
        try {
            ByteBufInputStream(byteBuf).use { bufInputStream ->
                NBTInputStream(bufInputStream, ByteOrder.LITTLE_ENDIAN).use { nbtInputStream ->
                    val bVersion = readBitArrayVersion(byteBuf)
                    if (bVersion == BitArrayVersion.V0) {
                        this.bitArray = bVersion.createArray(SubChunk.SIZE)
                        palette.clear()
                        addBlockPalette(byteBuf, deserializer, nbtInputStream)
                        this.onResize(BitArrayVersion.V2)
                        return
                    }
                    readWords(byteBuf, bVersion!!)
                    val paletteSize = byteBuf.readIntLE()
                    for (i in 0..<paletteSize) {
                        addBlockPalette(byteBuf, deserializer, nbtInputStream)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun writeToStorageRuntime(byteBuf: ByteBuf, serializer: RuntimeDataSerializer<V>, last: Palette<V>?) {
        if (writeLast(byteBuf, last)) return
        if (writeEmpty(byteBuf, serializer)) return

        byteBuf.writeByte(getPaletteHeader(bitArray.version, true))
        for (word in bitArray.words) byteBuf.writeIntLE(word)
        byteBuf.writeIntLE(palette.size)
        for (value in this.palette) byteBuf.writeIntLE(serializer.serialize(value))
    }

    fun readFromStorageRuntime(byteBuf: ByteBuf, deserializer: RuntimeDataDeserializer<V>, last: Palette<V>?) {
        val header = byteBuf.readUnsignedByte()

        if (hasCopyLastFlag(header)) {
            last?.copyTo(this)
            return
        }

        val version = getVersionFromPaletteHeader(header)
        if (version == BitArrayVersion.V0) {
            this.bitArray = version.createArray(SubChunk.SIZE, IntArray(0))
            palette.clear()
            palette.add(deserializer.deserialize(byteBuf.readIntLE()))

            this.onResize(BitArrayVersion.V2)
            return
        }

        readWords(byteBuf, version!!)

        val paletteSize = byteBuf.readIntLE()
        for (i in 0..<paletteSize) palette.add(deserializer.deserialize(byteBuf.readIntLE()))
    }

    fun paletteIndexFor(value: V): Int {
        var index = palette.indexOf(value)
        if (index != -1) return index

        index = palette.size
        palette.add(value)

        val version = bitArray.version
        if (index > version.maxEntryValue) {
            val next = version.next
            if (next != null) this.onResize(next)
        }

        return index
    }

    val isEmpty: Boolean
        get() {
            if (palette.size == 1) {
                for (word in bitArray.words) if (word.toLong() != 0L) {
                    return false
                }
                return true
            } else return false
        }

    @Throws(IOException::class)
    protected fun addBlockPalette(
        byteBuf: ByteBuf,
        deserializer: RuntimeDataDeserializer<V>,
        input: NBTInputStream
    ) {
        val p = PaletteUtils.fastReadBlockHash(input, byteBuf) //depend on LinkCompoundTag

        @Suppress("UNCHECKED_CAST")
        val unknownState = BlockUnknown.properties.defaultState as V

        if (p == null) {
            palette.add(unknownState)
            return
        }

        var resultingBlockState: V? = unknownState
        var semVersion = p.second

        if (semVersion == null) {
            semVersion = ProtocolInfo.GAME_VERSION
        }

        val version: Int =
            CompoundTagUpdaterContext.makeVersion(semVersion.major, semVersion.minor, semVersion.patch)

        var isBlockOutdated = false

        if (p.first == null) {     // is blockStateHash null
            isBlockOutdated = true
        } else {
            val hash = p.first!!
            val currentState = deserializer.deserialize(hash)
            if (hash != -2 && currentState === unknownState) {
                byteBuf.resetReaderIndex()
                isBlockOutdated = true
            } else {
                resultingBlockState = currentState
            }
        }

        if (isBlockOutdated) {
            val oldBlockNbt = input.readTag() as CompoundTag
            val newNbtMap = BlockStateUpdaters.updateBlockState(oldBlockNbt, version)
            val states = TreeMapCompoundTag(newNbtMap.getCompound("states").tags)

            val newBlockNbt = CompoundTag()
                .putString("name", newNbtMap.getString("name"))
                .putCompound("states", states)

            val hash = HashUtils.fnv1a_32_nbt(newBlockNbt)

            resultingBlockState = deserializer.deserialize(hash)

            // we send a warning if the resultingBlockState is null or unknown after updating it.
            // this way the only possibility is that the block hash is not represented in block_palette.nbt
            if (resultingBlockState == null || resultingBlockState === unknownState) {
                resultingBlockState = unknownState
                log.warn(
                    "missing block palette, blockHash: {}, blockId {}",
                    hash,
                    oldBlockNbt.getString("name")
                )
            }
        }

        if (resultingBlockState === unknownState) {
            val replaceWithUnknown: Boolean = Server.instance.settings.baseSettings.saveUnknownBlock
            if (replaceWithUnknown) {
                palette.add(resultingBlockState)
            }
        } else if (resultingBlockState != null) {
            palette.add(resultingBlockState)
        }
    }


    protected fun readBitArrayVersion(byteBuf: ByteBuf): BitArrayVersion? {
        val header = byteBuf.readUnsignedByte()
        return getVersionFromPaletteHeader(header)
    }

    protected fun readWords(byteBuf: ByteBuf, version: BitArrayVersion) {
        val wordCount = version.getWordsForSize(SubChunk.SIZE)
        val words = IntArray(wordCount)
        for (i in 0..<wordCount) words[i] = byteBuf.readIntLE()

        this.bitArray = version.createArray(SubChunk.SIZE, words)
        palette.clear()
    }

    protected fun onResize(version: BitArrayVersion) {
        val newBitArray = version.createArray(SubChunk.SIZE)
        for (i in 0..<SubChunk.SIZE) newBitArray[i] = bitArray[i]

        this.bitArray = newBitArray
    }

    fun copyTo(palette: Palette<V>) {
        palette.bitArray = bitArray.copy()
        palette.palette.clear()
        palette.palette.addAll(this.palette)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Palette<*>) return false
        return Objects.equal(palette, other.palette) && Objects.equal(bitArray, other.bitArray)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(palette, bitArray)
    }

    companion object : Loggable {
        protected const val COPY_LAST_FLAG_HEADER: Byte = ((0x7F shl 1).toByte().toInt() or 1).toByte()
        fun hasCopyLastFlag(header: Short): Boolean {
            return (header.toInt() shr 1) == 0x7F
        }

        fun isPersistent(header: Short): Boolean {
            return (header.toInt() and 1) == 0
        }

        fun getPaletteHeader(version: BitArrayVersion, runtime: Boolean): Int {
            return (version.bits.toInt() shl 1) or (if (runtime) 1 else 0)
        }

        fun getVersionFromPaletteHeader(header: Short): BitArrayVersion? {
            return BitArrayVersion.get(header.toInt() shr 1, true)
        }
    }
}