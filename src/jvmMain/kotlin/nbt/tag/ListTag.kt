package org.chorus_oss.chorus.nbt.tag

import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.max

class ListTag<T : Tag<*>> : Tag<MutableList<T>> {
    private var list: MutableList<T> = ArrayList()

    var type: Byte = 0

    constructor()

    constructor(type: Int) {
        this.type = type.toByte()
    }

    constructor(tags: Collection<T>) {
        val first = tags.stream().findFirst()
        require(!first.isEmpty) { "tags cannot be empty" }
        type = tags.stream().findFirst().get().id
        list.addAll(tags)
    }

    constructor(type: Int, tags: Collection<T>) {
        this.type = type.toByte()
        list.addAll(tags)
    }

    override val id: Byte
        get() = TAG_LIST

    override fun toString(): String {
        val joiner = StringJoiner(",\n\t")
        list.forEach(Consumer { tag: T -> joiner.add(tag.toString().replace("\n", "\n\t")) })
        return "ListTag (" + list.size + " entries of type " + getTagName(type) + ") {\n\t" + joiner + "\n}"
    }

    override fun toSNBT(): String {
        return "[" + list.stream()
            .map { obj: T -> obj.toSNBT() }
            .collect(Collectors.joining(",")) + "]"
    }

    override fun toSNBT(space: Int): String {
        val addSpace = StringBuilder()
        addSpace.append(" ".repeat(max(0.0, space.toDouble()).toInt()))
        if (list.isEmpty()) {
            return "[]"
        } else if (list[0] is StringTag || list[0] is CompoundTag || list[0] is ListTag<*>) {
            val joiner1 = StringJoiner(",\n$addSpace")
            list.forEach(Consumer { tag: T ->
                joiner1.add(
                    tag.toSNBT(space).replace(
                        "\n", """
     
     $addSpace
     """.trimIndent()
                    )
                )
            })
            return "[\n$addSpace$joiner1\n]"
        } else {
            val joiner2 = StringJoiner(", ")
            list.forEach(Consumer { tag: T -> joiner2.add(tag.toSNBT(space)) })
            return "[$joiner2]"
        }
    }

    fun add(tag: T): ListTag<T> {
        type = tag.id
        list.add(tag)
        return this
    }

    fun add(index: Int, tag: T): ListTag<T> {
        type = tag.id

        if (index >= list.size) {
            list.add(index, tag)
        } else {
            list[index] = tag
        }
        return this
    }

    override fun parseValue(): MutableList<T> {
        val value: MutableList<T> = ArrayList(list.size)

        for (t in this.list) {
            @Suppress("UNCHECKED_CAST")
            value.add(t.parseValue() as? T ?: continue)
        }

        return value
    }

    operator fun get(index: Int): T {
        return list[index]
    }

    var all: List<T>
        get() = ArrayList(list)
        set(tags) {
            this.list = ArrayList(tags)
        }

    fun remove(tag: T) {
        list.remove(tag)
    }

    fun remove(index: Int) {
        list.removeAt(index)
    }

    fun removeAll(tags: Collection<T>) {
        list.removeAll(tags)
    }

    fun size(): Int {
        return list.size
    }

    override fun copy(): Tag<MutableList<T>> {
        val res = ListTag<T>()
        res.type = type
        for (t in list) {
            @Suppress("UNCHECKED_CAST")
            val copy: T = t.copy() as? T ?: continue
            res.list.add(copy)
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as ListTag<*>
            if (type == o.type) {
                return list == o.list
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), type, list)
    }
}
