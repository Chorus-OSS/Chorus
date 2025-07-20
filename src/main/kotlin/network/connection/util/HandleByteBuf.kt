package org.chorus_oss.chorus.network.connection.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ByteProcessor
import io.netty.util.internal.ObjectUtil
import io.netty.util.internal.StringUtil
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.nbt.tag.StringTag
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset

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
