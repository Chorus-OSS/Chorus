package org.chorus_oss.chorus.network.connection.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ByteProcessor
import io.netty.util.internal.ObjectUtil
import io.netty.util.internal.StringUtil
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemDurable
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.NBTIO.read
import org.chorus_oss.chorus.nbt.NBTIO.write
import org.chorus_oss.chorus.nbt.stream.LittleEndianByteBufInputStreamNBTInputStream
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.recipe.descriptor.*
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.BiConsumer

class HandleByteBuf private constructor(buf: ByteBuf) : ByteBuf() {
    private val buf: ByteBuf = ObjectUtil.checkNotNull(buf, "buf")

    override fun hasMemoryAddress(): Boolean {
        return buf.hasMemoryAddress()
    }

    override fun isContiguous(): Boolean {
        return buf.isContiguous
    }

    override fun memoryAddress(): Long {
        return buf.memoryAddress()
    }

    override fun capacity(): Int {
        return buf.capacity()
    }

    override fun capacity(newCapacity: Int): ByteBuf {
        buf.capacity(newCapacity)
        return this
    }

    override fun maxCapacity(): Int {
        return buf.maxCapacity()
    }

    override fun alloc(): ByteBufAllocator {
        return buf.alloc()
    }

    @Deprecated("Deprecated in Java")
    override fun order(): ByteOrder {
        @Suppress("DEPRECATION")
        return buf.order()
    }

    @Deprecated("Deprecated in Java")
    override fun order(endianness: ByteOrder): ByteBuf {
        @Suppress("DEPRECATION")
        return buf.order(endianness)
    }

    override fun unwrap(): ByteBuf {
        return buf
    }

    override fun asReadOnly(): ByteBuf {
        return buf.asReadOnly()
    }

    override fun isReadOnly(): Boolean {
        return buf.isReadOnly
    }

    override fun isDirect(): Boolean {
        return buf.isDirect
    }

    override fun readerIndex(): Int {
        return buf.readerIndex()
    }

    override fun readerIndex(readerIndex: Int): ByteBuf {
        buf.readerIndex(readerIndex)
        return this
    }

    override fun writerIndex(): Int {
        return buf.writerIndex()
    }

    override fun writerIndex(writerIndex: Int): ByteBuf {
        buf.writerIndex(writerIndex)
        return this
    }

    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf {
        buf.setIndex(readerIndex, writerIndex)
        return this
    }

    override fun readableBytes(): Int {
        return buf.readableBytes()
    }

    override fun writableBytes(): Int {
        return buf.writableBytes()
    }

    override fun maxWritableBytes(): Int {
        return buf.maxWritableBytes()
    }

    override fun maxFastWritableBytes(): Int {
        return buf.maxFastWritableBytes()
    }

    override fun isReadable(): Boolean {
        return buf.isReadable
    }

    override fun isWritable(): Boolean {
        return buf.isWritable
    }

    override fun clear(): ByteBuf {
        buf.clear()
        return this
    }

    override fun markReaderIndex(): ByteBuf {
        buf.markReaderIndex()
        return this
    }

    override fun resetReaderIndex(): ByteBuf {
        buf.resetReaderIndex()
        return this
    }

    override fun markWriterIndex(): ByteBuf {
        buf.markWriterIndex()
        return this
    }

    override fun resetWriterIndex(): ByteBuf {
        buf.resetWriterIndex()
        return this
    }

    override fun discardReadBytes(): ByteBuf {
        buf.discardReadBytes()
        return this
    }

    override fun discardSomeReadBytes(): ByteBuf {
        buf.discardSomeReadBytes()
        return this
    }

    override fun ensureWritable(minWritableBytes: Int): ByteBuf {
        buf.ensureWritable(minWritableBytes)
        return this
    }

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int {
        return buf.ensureWritable(minWritableBytes, force)
    }

    override fun getBoolean(index: Int): Boolean {
        return buf.getBoolean(index)
    }

    override fun getByte(index: Int): Byte {
        return buf.getByte(index)
    }

    override fun getUnsignedByte(index: Int): Short {
        return buf.getUnsignedByte(index)
    }

    override fun getShort(index: Int): Short {
        return buf.getShort(index)
    }

    override fun getShortLE(index: Int): Short {
        return buf.getShortLE(index)
    }

    override fun getUnsignedShort(index: Int): Int {
        return buf.getUnsignedShort(index)
    }

    override fun getUnsignedShortLE(index: Int): Int {
        return buf.getUnsignedShortLE(index)
    }

    override fun getMedium(index: Int): Int {
        return buf.getMedium(index)
    }

    override fun getMediumLE(index: Int): Int {
        return buf.getMediumLE(index)
    }

    override fun getUnsignedMedium(index: Int): Int {
        return buf.getUnsignedMedium(index)
    }

    override fun getUnsignedMediumLE(index: Int): Int {
        return buf.getUnsignedMediumLE(index)
    }

    override fun getInt(index: Int): Int {
        return buf.getInt(index)
    }

    override fun getIntLE(index: Int): Int {
        return buf.getIntLE(index)
    }

    override fun getUnsignedInt(index: Int): Long {
        return buf.getUnsignedInt(index)
    }

    override fun getUnsignedIntLE(index: Int): Long {
        return buf.getUnsignedIntLE(index)
    }

    override fun getLong(index: Int): Long {
        return buf.getLong(index)
    }

    override fun getLongLE(index: Int): Long {
        return buf.getLongLE(index)
    }

    override fun getChar(index: Int): Char {
        return buf.getChar(index)
    }

    override fun getFloat(index: Int): Float {
        return buf.getFloat(index)
    }

    override fun getDouble(index: Int): Double {
        return buf.getDouble(index)
    }

    override fun getBytes(index: Int, dst: ByteBuf): ByteBuf {
        buf.getBytes(index, dst)
        return this
    }

    override fun getBytes(index: Int, dst: ByteBuf, length: Int): ByteBuf {
        buf.getBytes(index, dst, length)
        return this
    }

    override fun getBytes(index: Int, dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        buf.getBytes(index, dst, dstIndex, length)
        return this
    }

    override fun getBytes(index: Int, dst: ByteArray): ByteBuf {
        buf.getBytes(index, dst)
        return this
    }

    override fun getBytes(index: Int, dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        buf.getBytes(index, dst, dstIndex, length)
        return this
    }

    override fun getBytes(index: Int, dst: ByteBuffer): ByteBuf {
        buf.getBytes(index, dst)
        return this
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: OutputStream, length: Int): ByteBuf {
        buf.getBytes(index, out, length)
        return this
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: GatheringByteChannel, length: Int): Int {
        return buf.getBytes(index, out, length)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: FileChannel, position: Long, length: Int): Int {
        return buf.getBytes(index, out, position, length)
    }

    override fun getCharSequence(index: Int, length: Int, charset: Charset): CharSequence {
        return buf.getCharSequence(index, length, charset)
    }

    override fun setBoolean(index: Int, value: Boolean): ByteBuf {
        buf.setBoolean(index, value)
        return this
    }

    override fun setByte(index: Int, value: Int): ByteBuf {
        buf.setByte(index, value)
        return this
    }

    override fun setShort(index: Int, value: Int): ByteBuf {
        buf.setShort(index, value)
        return this
    }

    override fun setShortLE(index: Int, value: Int): ByteBuf {
        buf.setShortLE(index, value)
        return this
    }

    override fun setMedium(index: Int, value: Int): ByteBuf {
        buf.setMedium(index, value)
        return this
    }

    override fun setMediumLE(index: Int, value: Int): ByteBuf {
        buf.setMediumLE(index, value)
        return this
    }

    override fun setInt(index: Int, value: Int): ByteBuf {
        buf.setInt(index, value)
        return this
    }

    override fun setIntLE(index: Int, value: Int): ByteBuf {
        buf.setIntLE(index, value)
        return this
    }

    override fun setLong(index: Int, value: Long): ByteBuf {
        buf.setLong(index, value)
        return this
    }

    override fun setLongLE(index: Int, value: Long): ByteBuf {
        buf.setLongLE(index, value)
        return this
    }

    override fun setChar(index: Int, value: Int): ByteBuf {
        buf.setChar(index, value)
        return this
    }

    override fun setFloat(index: Int, value: Float): ByteBuf {
        buf.setFloat(index, value)
        return this
    }

    override fun setDouble(index: Int, value: Double): ByteBuf {
        buf.setDouble(index, value)
        return this
    }

    override fun setBytes(index: Int, src: ByteBuf): ByteBuf {
        buf.setBytes(index, src)
        return this
    }

    override fun setBytes(index: Int, src: ByteBuf, length: Int): ByteBuf {
        buf.setBytes(index, src, length)
        return this
    }

    override fun setBytes(index: Int, src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        buf.setBytes(index, src, srcIndex, length)
        return this
    }

    override fun setBytes(index: Int, src: ByteArray): ByteBuf {
        buf.setBytes(index, src)
        return this
    }

    override fun setBytes(index: Int, src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        buf.setBytes(index, src, srcIndex, length)
        return this
    }

    override fun setBytes(index: Int, src: ByteBuffer): ByteBuf {
        buf.setBytes(index, src)
        return this
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: InputStream, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: ScatteringByteChannel, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: FileChannel, position: Long, length: Int): Int {
        return buf.setBytes(index, `in`, position, length)
    }

    override fun setZero(index: Int, length: Int): ByteBuf {
        buf.setZero(index, length)
        return this
    }

    override fun setCharSequence(index: Int, sequence: CharSequence, charset: Charset): Int {
        return buf.setCharSequence(index, sequence, charset)
    }

    override fun readBoolean(): Boolean {
        return buf.readBoolean()
    }

    override fun readByte(): Byte {
        return buf.readByte()
    }

    override fun readUnsignedByte(): Short {
        return buf.readUnsignedByte()
    }

    override fun readShort(): Short {
        return buf.readShort()
    }

    override fun readShortLE(): Short {
        return buf.readShortLE()
    }

    override fun readUnsignedShort(): Int {
        return buf.readUnsignedShort()
    }

    override fun readUnsignedShortLE(): Int {
        return buf.readUnsignedShortLE()
    }

    override fun readMedium(): Int {
        return buf.readMedium()
    }

    override fun readMediumLE(): Int {
        return buf.readMediumLE()
    }

    override fun readUnsignedMedium(): Int {
        return buf.readUnsignedMedium()
    }

    override fun readUnsignedMediumLE(): Int {
        return buf.readUnsignedMediumLE()
    }

    override fun readInt(): Int {
        return buf.readInt()
    }

    override fun readIntLE(): Int {
        return buf.readIntLE()
    }

    override fun readUnsignedInt(): Long {
        return buf.readUnsignedInt()
    }

    override fun readUnsignedIntLE(): Long {
        return buf.readUnsignedIntLE()
    }

    override fun readLong(): Long {
        return buf.readLong()
    }

    override fun readLongLE(): Long {
        return buf.readLongLE()
    }

    override fun readChar(): Char {
        return buf.readChar()
    }

    override fun readFloat(): Float {
        return buf.readFloat()
    }

    override fun readDouble(): Double {
        return buf.readDouble()
    }

    override fun readBytes(length: Int): ByteBuf {
        return buf.readBytes(length)
    }

    override fun readSlice(length: Int): ByteBuf {
        return buf.readSlice(length)
    }

    override fun readRetainedSlice(length: Int): ByteBuf {
        return buf.readRetainedSlice(length)
    }

    override fun readBytes(dst: ByteBuf): ByteBuf {
        buf.readBytes(dst)
        return this
    }

    override fun readBytes(dst: ByteBuf, length: Int): ByteBuf {
        buf.readBytes(dst, length)
        return this
    }

    override fun readBytes(dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        buf.readBytes(dst, dstIndex, length)
        return this
    }

    override fun readBytes(dst: ByteArray): ByteBuf {
        buf.readBytes(dst)
        return this
    }

    override fun readBytes(dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        buf.readBytes(dst, dstIndex, length)
        return this
    }

    override fun readBytes(dst: ByteBuffer): ByteBuf {
        buf.readBytes(dst)
        return this
    }

    @Throws(IOException::class)
    override fun readBytes(out: OutputStream, length: Int): ByteBuf {
        buf.readBytes(out, length)
        return this
    }

    @Throws(IOException::class)
    override fun readBytes(out: GatheringByteChannel, length: Int): Int {
        return buf.readBytes(out, length)
    }

    @Throws(IOException::class)
    override fun readBytes(out: FileChannel, position: Long, length: Int): Int {
        return buf.readBytes(out, position, length)
    }

    override fun readCharSequence(length: Int, charset: Charset): CharSequence {
        return buf.readCharSequence(length, charset)
    }

    override fun skipBytes(length: Int): ByteBuf {
        buf.skipBytes(length)
        return this
    }

    override fun writeBoolean(value: Boolean): ByteBuf {
        buf.writeBoolean(value)
        return this
    }

    override fun writeByte(value: Int): ByteBuf {
        buf.writeByte(value)
        return this
    }

    override fun writeShort(value: Int): ByteBuf {
        buf.writeShort(value)
        return this
    }

    override fun writeShortLE(value: Int): ByteBuf {
        buf.writeShortLE(value)
        return this
    }

    override fun writeMedium(value: Int): ByteBuf {
        buf.writeMedium(value)
        return this
    }

    override fun writeMediumLE(value: Int): ByteBuf {
        buf.writeMediumLE(value)
        return this
    }

    override fun writeInt(value: Int): ByteBuf {
        buf.writeInt(value)
        return this
    }

    override fun writeIntLE(value: Int): ByteBuf {
        buf.writeIntLE(value)
        return this
    }

    override fun writeLong(value: Long): ByteBuf {
        buf.writeLong(value)
        return this
    }

    override fun writeLongLE(value: Long): ByteBuf {
        buf.writeLongLE(value)
        return this
    }

    override fun writeChar(value: Int): ByteBuf {
        buf.writeChar(value)
        return this
    }

    override fun writeFloat(value: Float): ByteBuf {
        buf.writeFloat(value)
        return this
    }

    override fun writeDouble(value: Double): ByteBuf {
        buf.writeDouble(value)
        return this
    }

    override fun writeBytes(src: ByteBuf): ByteBuf {
        buf.writeBytes(src)
        return this
    }

    override fun writeBytes(src: ByteBuf, length: Int): ByteBuf {
        buf.writeBytes(src, length)
        return this
    }

    override fun writeBytes(src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        buf.writeBytes(src, srcIndex, length)
        return this
    }

    override fun writeBytes(src: ByteArray): ByteBuf {
        buf.writeBytes(src)
        return this
    }

    override fun writeBytes(src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        buf.writeBytes(src, srcIndex, length)
        return this
    }

    override fun writeBytes(src: ByteBuffer): ByteBuf {
        buf.writeBytes(src)
        return this
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: InputStream, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: ScatteringByteChannel, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: FileChannel, position: Long, length: Int): Int {
        return buf.writeBytes(`in`, position, length)
    }

    override fun writeZero(length: Int): ByteBuf {
        buf.writeZero(length)
        return this
    }

    override fun writeCharSequence(sequence: CharSequence, charset: Charset): Int {
        return buf.writeCharSequence(sequence, charset)
    }

    fun readString(): String {
        val bytes = ByteArray(this.readUnsignedVarInt())
        buf.readBytes(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun writeString(str: String): ByteBuf {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        this.writeUnsignedVarInt(bytes.size)
        return buf.writeBytes(bytes)
    }

    fun writeUUID(uuid: UUID) {
        this.writeBytes(Binary.writeUUID(uuid))
    }

    fun readUUID(): UUID {
        val bytes = ByteArray(16)
        this.readBytes(bytes)
        return Binary.readUUID(bytes)
    }

    fun writeImage(image: SerializedImage) {
        this.writeIntLE(image.width)
        this.writeIntLE(image.height)
        this.writeUnsignedVarInt(image.data.size)
        this.writeBytes(image.data)
    }

    fun readImage(): SerializedImage {
        val width = this.readIntLE()
        val height = this.readIntLE()
        val bytes = ByteArray(this.readUnsignedVarInt())
        this.readBytes(bytes)
        return SerializedImage(width, height, bytes)
    }

    fun readByteArray(): ByteArray {
        val bytes = ByteArray(readUnsignedVarInt())
        readBytes(bytes)
        return bytes
    }

    fun writeByteArray(bytes: ByteArray) {
        writeUnsignedVarInt(bytes.size)
        this.writeBytes(bytes)
    }

    fun writeSkin(skin: Skin) {
        this.writeString(skin.getSkinId())
        this.writeString(skin.getPlayFabId())
        this.writeString(skin.getSkinResourcePatch())
        this.writeImage(skin.getSkinData())

        val animations = skin.getAnimations()
        this.writeIntLE(animations.size)
        for (animation in animations) {
            this.writeImage(animation.image)
            this.writeIntLE(animation.type)
            this.writeFloatLE(animation.frames)
            this.writeIntLE(animation.expression)
        }

        this.writeImage(skin.getCapeData())
        this.writeString(skin.getGeometryData())
        this.writeString(skin.getGeometryDataEngineVersion()!!)
        this.writeString(skin.getAnimationData())
        this.writeString(skin.getCapeId())
        this.writeString(skin.getFullSkinId())
        this.writeString(skin.getArmSize()!!)
        this.writeString(skin.getSkinColor()!!)
        val pieces = skin.getPersonaPieces()
        this.writeIntLE(pieces.size)
        for (piece in pieces) {
            this.writeString(piece.id)
            this.writeString(piece.type)
            this.writeString(piece.packId)
            this.writeBoolean(piece.isDefault)
            this.writeString(piece.productId)
        }

        val tints = skin.getTintColors()
        this.writeIntLE(tints.size)
        for (tint in tints) {
            this.writeString(tint.pieceType)
            val colors: List<String> = tint.colors
            this.writeIntLE(colors.size)
            for (color in colors) {
                this.writeString(color)
            }
        }

        this.writeBoolean(skin.isPremium())
        this.writeBoolean(skin.isPersona())
        this.writeBoolean(skin.isCapeOnClassic())
        this.writeBoolean(skin.isPrimaryUser())
        this.writeBoolean(skin.isOverridingPlayerAppearance())
    }

    fun writeUnsignedVarInt(value: Int) {
        ByteBufVarInt.writeUnsignedInt(this, value)
    }

    fun readUnsignedVarInt(): Int {
        return ByteBufVarInt.readUnsignedInt(this)
    }

    fun writeVarInt(value: Int) {
        ByteBufVarInt.writeInt(this, value)
    }

    fun readVarInt(): Int {
        return ByteBufVarInt.readInt(this)
    }

    fun writeUnsignedVarLong(value: Long) {
        ByteBufVarInt.writeUnsignedLong(this, value)
    }

    fun readUnsignedVarLong(): Long {
        return ByteBufVarInt.readUnsignedLong(this)
    }

    fun writeVarLong(value: Long) {
        ByteBufVarInt.writeLong(this, value)
    }

    fun readSlot(): Item {
        return this.readSlot(false)
    }

    fun readSlot(instanceItem: Boolean): Item {
        val runtimeId = this.readVarInt()
        if (runtimeId == 0) {
            return Item.AIR
        }

        val count = readShortLE().toInt()
        val damage = readUnsignedVarInt()

        var netId: Int? = null
        if (!instanceItem) {
            val hasNetId = readBoolean()
            if (hasNetId) {
                netId = this.readVarInt()
            }
        }
        val blockRuntimeId = this.readVarInt()

        var compoundTag: CompoundTag? = null
        val canPlace: Array<String>
        val canBreak: Array<String>
        val item: Item
        if (blockRuntimeId == 0) {
            item = get(Registries.ITEM_RUNTIMEID.getIdentifier(runtimeId), damage, count)
        } else {
            item = get(Registries.ITEM_RUNTIMEID.getIdentifier(runtimeId), damage, count)
            val blockState = Registries.BLOCKSTATE[blockRuntimeId]
            if (blockState != null) {
                item.blockState = blockState
            }
        }

        if (netId != null) {
            item.netId = (netId)
        }

        val bytes = ByteArray(readUnsignedVarInt())
        readBytes(bytes)
        val buf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.size)
        buf.writeBytes(bytes)
        try {
            LittleEndianByteBufInputStream(buf).use { stream ->
                val nbtSize = stream.readShort().toInt()
                if (nbtSize > 0) {
                    val ls = LittleEndianByteBufInputStreamNBTInputStream(stream)
                    compoundTag = ls.readTag() as CompoundTag?
                } else if (nbtSize == -1) {
                    val tagCount = stream.readUnsignedByte()
                    require(tagCount == 1) { "Expected 1 tag but got $tagCount" }
                    val ls = LittleEndianByteBufInputStreamNBTInputStream(stream)
                    compoundTag = ls.readTag() as CompoundTag?
                }

                canPlace = Array(stream.readInt()) {
                    stream.readUTF()
                }

                canBreak = Array(stream.readInt()) {
                    stream.readUTF()
                }

                if (item.id == ItemID.SHIELD) {
                    stream.readLong() // blockingTicks
                }
                if (compoundTag != null) {
                    if (compoundTag.contains("__DamageConflict__")) {
                        compoundTag.put("Damage", compoundTag.removeAndGet("__DamageConflict__")!!)
                    }
                    item.setCompoundTag(compoundTag)
                }
                val canPlaces = canPlace.map { Block.get(it) }.toTypedArray()
                if (canPlaces.isNotEmpty()) {
                    item.setCanPlaceOn(canPlaces)
                }

                val canBreaks = canBreak.map { Block.get(it) }.toTypedArray()
                if (canBreaks.isNotEmpty()) {
                    item.setCanPlaceOn(canBreaks)
                }
                return item
            }
        } catch (e: IOException) {
            throw IllegalStateException("Unable to read item user data", e)
        } finally {
            buf.release()
        }
    }

    @JvmOverloads
    fun writeSlot(item: Item?, instanceItem: Boolean = false) {
        if (item == null || item.isNothing) {
            writeByte(0.toByte().toInt())
            return
        }

        val networkId = item.runtimeId
        writeVarInt(networkId) //write item runtimeId
        writeShortLE(item.getCount()) //write item count
        writeUnsignedVarInt(item.damage) //write damage value


        if (!instanceItem) {
            writeBoolean(item.isUsingNetId) // isUsingNetId
            if (item.isUsingNetId) {
                writeVarInt(item.getNetId()) // netId
            }
        }

        writeVarInt(if (item.isBlock()) item.getSafeBlockState().blockStateHash() else 0)

        val userDataBuf = ByteBufAllocator.DEFAULT.ioBuffer()
        try {
            LittleEndianByteBufOutputStream(userDataBuf).use { stream ->
                val data = item.damage
                if (item is ItemDurable && data != 0) {
                    val nbt = item.compoundTag
                    val tag = if (nbt.isEmpty()) {
                        CompoundTag()
                    } else {
                        read(nbt, ByteOrder.LITTLE_ENDIAN)
                    }
                    if (tag.contains("Damage")) {
                        tag.put("__DamageConflict__", tag.removeAndGet("Damage")!!)
                    }
                    tag.putInt("Damage", data)
                    stream.writeShort(-1)
                    stream.writeByte(1) // Hardcoded in current version
                    stream.write(write(tag, ByteOrder.LITTLE_ENDIAN))
                } else if (item.hasCompoundTag()) {
                    stream.writeShort(-1)
                    stream.writeByte(1) // Hardcoded in current version
                    stream.write(write(item.namedTag!!, ByteOrder.LITTLE_ENDIAN))
                } else {
                    userDataBuf.writeShortLE(0)
                }

                val canPlaceOn = extractStringList(item, "CanPlaceOn") //write canPlace
                stream.writeInt(canPlaceOn.size)
                for (string in canPlaceOn) {
                    stream.writeUTF(string)
                }

                val canDestroy = extractStringList(item, "CanDestroy") //write canBreak
                stream.writeInt(canDestroy.size)
                for (string in canDestroy) {
                    stream.writeUTF(string)
                }

                if (item.id == ItemID.SHIELD) {
                    stream.writeLong(0) //BlockingTicks // todo add BlockingTicks to Item Class. Find out what Blocking Ticks are
                }

                val bytes = Utils.convertByteBuf2Array(userDataBuf)
                writeByteArray(bytes)
            }
        } catch (e: IOException) {
            throw IllegalStateException("Unable to write item user data", e)
        } finally {
            userDataBuf.release()
        }
    }

    fun writeRecipeIngredient(itemDescriptor: ItemDescriptor) {
        val type = itemDescriptor.type
        this.writeByte(type.ordinal.toByte().toInt())
        when (type) {
            ItemDescriptorType.DEFAULT -> {
                val ingredient = itemDescriptor.toItem()
                if (ingredient.isNothing) {
                    this.writeShortLE(0)
                    this.writeVarInt(0) // item == null ? 0 : item.getCount()
                    return
                }
                val networkId = ingredient.runtimeId
                val damage = if (ingredient.hasMeta()) ingredient.damage else 0x7fff
                this.writeShortLE(networkId)
                this.writeShortLE(damage)
            }

            ItemDescriptorType.MOLANG -> {
                val molangDescriptor = itemDescriptor as MolangDescriptor
                this.writeString(molangDescriptor.tagExpression)
                this.writeByte(molangDescriptor.molangVersion.toByte().toInt())
            }

            ItemDescriptorType.COMPLEX_ALIAS -> {
                val complexAliasDescriptor = itemDescriptor as ComplexAliasDescriptor
                this.writeString(complexAliasDescriptor.name)
            }

            ItemDescriptorType.ITEM_TAG -> {
                val tagDescriptor = itemDescriptor as ItemTagDescriptor
                this.writeString(tagDescriptor.itemTag)
            }

            ItemDescriptorType.DEFERRED -> {
                val deferredDescriptor = itemDescriptor as DeferredDescriptor
                this.writeString(deferredDescriptor.fullName)
                this.writeShortLE(deferredDescriptor.auxValue)
            }

            else -> Unit
        }

        this.writeVarInt(itemDescriptor.count)
    }

    fun writeBlockVector3(x: Int, y: Int, z: Int) {
        this.writeVarInt(x)
        this.writeUnsignedVarInt(y)
        this.writeVarInt(z)
    }

    fun readVector3f(): Vector3f {
        return Vector3f(this.readFloatLE(), this.readFloatLE(), this.readFloatLE())
    }

    fun writeVector3f(x: Float, y: Float, z: Float) {
        this.writeFloatLE(x)
        this.writeFloatLE(y)
        this.writeFloatLE(z)
    }

    fun writeGameRules(gameRules: GameRules) {
        val rules = gameRules.getGameRules().toMutableMap()
        rules.keys.removeIf(GameRule::isDeprecated)

        this.writeUnsignedVarInt(rules.size)
        rules.forEach { (gameRule, value) ->
            this.writeString(gameRule.gameRuleName.lowercase())
            value.write(this)
        }
    }

    fun writeActorUniqueID(actorUniqueID: Long) {
        this.writeVarLong(actorUniqueID)
    }

    fun readActorRuntimeID(): Long {
        return this.readUnsignedVarLong()
    }

    fun writeActorRuntimeID(actorRuntimeID: Long) {
        this.writeUnsignedVarLong(actorRuntimeID)
    }

    fun <T> writeArray(array: Collection<T>, biConsumer: BiConsumer<HandleByteBuf, T>) {
        this.writeUnsignedVarInt(array.size)
        for (`val` in array) {
            biConsumer.accept(this, `val`)
        }
    }

    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int {
        return buf.indexOf(fromIndex, toIndex, value)
    }

    override fun bytesBefore(value: Byte): Int {
        return buf.bytesBefore(value)
    }

    override fun bytesBefore(length: Int, value: Byte): Int {
        return buf.bytesBefore(length, value)
    }

    override fun bytesBefore(index: Int, length: Int, value: Byte): Int {
        return buf.bytesBefore(index, length, value)
    }

    override fun forEachByte(processor: ByteProcessor): Int {
        return buf.forEachByte(processor)
    }

    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor): Int {
        return buf.forEachByte(index, length, processor)
    }

    override fun forEachByteDesc(processor: ByteProcessor): Int {
        return buf.forEachByteDesc(processor)
    }

    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor): Int {
        return buf.forEachByteDesc(index, length, processor)
    }

    override fun copy(): ByteBuf {
        return buf.copy()
    }

    override fun copy(index: Int, length: Int): ByteBuf {
        return buf.copy(index, length)
    }

    override fun slice(): ByteBuf {
        return buf.slice()
    }

    override fun retainedSlice(): ByteBuf {
        return buf.retainedSlice()
    }

    override fun slice(index: Int, length: Int): ByteBuf {
        return buf.slice(index, length)
    }

    override fun retainedSlice(index: Int, length: Int): ByteBuf {
        return buf.retainedSlice(index, length)
    }

    override fun duplicate(): ByteBuf {
        return buf.duplicate()
    }

    override fun retainedDuplicate(): ByteBuf {
        return buf.retainedDuplicate()
    }

    override fun nioBufferCount(): Int {
        return buf.nioBufferCount()
    }

    override fun nioBuffer(): ByteBuffer {
        return buf.nioBuffer()
    }

    override fun nioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.nioBuffer(index, length)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return buf.nioBuffers()
    }

    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> {
        return buf.nioBuffers(index, length)
    }

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.internalNioBuffer(index, length)
    }

    override fun hasArray(): Boolean {
        return buf.hasArray()
    }

    override fun array(): ByteArray {
        return buf.array()
    }

    override fun arrayOffset(): Int {
        return buf.arrayOffset()
    }

    override fun toString(charset: Charset): String {
        return buf.toString(charset)
    }

    override fun toString(index: Int, length: Int, charset: Charset): String {
        return buf.toString(index, length, charset)
    }

    override fun hashCode(): Int {
        return buf.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return buf == other
    }

    override fun compareTo(other: ByteBuf): Int {
        return buf.compareTo(other)
    }

    override fun toString(): String {
        return StringUtil.simpleClassName(this) + '(' + buf.toString() + ')'
    }

    override fun retain(increment: Int): ByteBuf {
        buf.retain(increment)
        return this
    }

    override fun retain(): ByteBuf {
        buf.retain()
        return this
    }

    override fun touch(): ByteBuf {
        buf.touch()
        return this
    }

    override fun touch(hint: Any): ByteBuf {
        buf.touch(hint)
        return this
    }

    override fun isReadable(size: Int): Boolean {
        return buf.isReadable(size)
    }

    override fun isWritable(size: Int): Boolean {
        return buf.isWritable(size)
    }

    override fun refCnt(): Int {
        return buf.refCnt()
    }

    override fun release(): Boolean {
        return buf.release()
    }

    override fun release(decrement: Int): Boolean {
        return buf.release(decrement)
    }

    companion object {
        @JvmStatic
        fun of(buf: ByteBuf): HandleByteBuf {
            return HandleByteBuf(buf)
        }

        fun extractStringList(item: Item, tagName: String): List<String> {
            val namedTag = item.namedTag ?: return emptyList()
            val listTag = namedTag.getList(tagName, StringTag::class.java)
            return listTag.all.map { it.data }
        }
    }
}
