package org.chorus_oss.chorus.nbt.tag

import java.util.*

class CompoundTagView(private val delegate: CompoundTag) : CompoundTag() {
    override val allTags: Collection<Tag<*>>
        get() = Collections.unmodifiableCollection(delegate.allTags)

    override val id: Byte
        get() = delegate.id

    override fun put(name: String, tag: Tag<*>): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putByte(name: String, value: Int): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putShort(name: String, value: Int): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putInt(name: String, value: Int): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putLong(name: String, value: Long): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putFloat(name: String, value: Float): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putDouble(name: String, value: Double): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putString(name: String, value: String): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putByteArray(name: String, value: ByteArray): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putIntArray(name: String, value: IntArray): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putList(name: String, listTag: ListTag<*>): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putCompound(name: String, value: CompoundTag): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun putBoolean(string: String, value: Boolean): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun get(name: String): Tag<*>? {
        return delegate[name]
    }

    override fun contains(name: String): Boolean {
        return delegate.contains(name)
    }

    override fun containsCompound(name: String): Boolean {
        return delegate.containsCompound(name)
    }

    override fun containsString(name: String): Boolean {
        return delegate.containsString(name)
    }

    override fun containsIntArray(name: String): Boolean {
        return delegate.containsIntArray(name)
    }

    override fun containsByteArray(name: String): Boolean {
        return delegate.containsByteArray(name)
    }

    override fun containsNumber(name: String): Boolean {
        return delegate.containsNumber(name)
    }

    override fun containsList(name: String): Boolean {
        return delegate.containsList(name)
    }

    override fun containsList(name: String, type: Byte): Boolean {
        return delegate.containsList(name, type)
    }

    override fun containsByte(name: String): Boolean {
        return delegate.containsByte(name)
    }

    override fun containsShort(name: String): Boolean {
        return delegate.containsShort(name)
    }

    override fun containsInt(name: String): Boolean {
        return delegate.containsInt(name)
    }

    override fun containsDouble(name: String): Boolean {
        return delegate.containsDouble(name)
    }

    override fun containsFloat(name: String): Boolean {
        return delegate.containsFloat(name)
    }

    override fun remove(name: String): CompoundTag {
        throw UnsupportedOperationException()
    }

    override fun <T : Tag<*>> removeAndGet(name: String): T? {
        throw UnsupportedOperationException()
    }

    override fun getByte(name: String): Byte {
        return delegate.getByte(name)
    }

    override fun getShort(name: String): Short {
        return delegate.getShort(name)
    }

    override fun getInt(name: String): Int {
        return delegate.getInt(name)
    }

    override fun getLong(name: String): Long {
        return delegate.getLong(name)
    }

    override fun getFloat(name: String): Float {
        return delegate.getFloat(name)
    }

    override fun getDouble(name: String): Double {
        return delegate.getDouble(name)
    }

    override fun getString(name: String): String {
        return delegate.getString(name)
    }

    override fun getByteArray(name: String): ByteArray {
        return delegate.getByteArray(name)
    }

    override fun getIntArray(name: String): IntArray {
        return delegate.getIntArray(name)
    }

    override fun getCompound(name: String): CompoundTag {
        return delegate.getCompound(name)
    }

    override fun getList(name: String): ListTag<Tag<*>> {
        return delegate.getList(name)
    }

    override fun <T : Tag<*>> getList(name: String, type: Class<T>): ListTag<T> {
        return delegate.getList(name, type)
    }

    override val tags: MutableMap<String, Tag<*>>
        get() {
            return delegate.tags
        }

    override fun parseValue(): MutableMap<String, Any> {
        return delegate.parseValue()
    }

    override fun getBoolean(name: String): Boolean {
        return delegate.getBoolean(name)
    }

    override fun toString(): String {
        return delegate.toString()
    }

    override fun toSNBT(): String {
        return delegate.toSNBT()
    }

    override fun toSNBT(space: Int): String {
        return delegate.toSNBT(space)
    }


    override val isEmpty: Boolean
        get() = delegate.isEmpty

    override fun copy(): LinkedCompoundTag {
        return LinkedCompoundTag(delegate.copy().tags)
    }

    override fun equals(other: Any?): Boolean {
        return delegate == other
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun exist(name: String): Boolean {
        return delegate.exist(name)
    }
}
