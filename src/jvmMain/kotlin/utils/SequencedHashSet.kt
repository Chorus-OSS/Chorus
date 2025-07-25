package org.chorus_oss.chorus.utils

class SequencedHashSet<E> : MutableList<E> {
    private val map: MutableMap<E, Int> = mutableMapOf()
    private val inverse: MutableMap<Int, E> = mutableMapOf()
    private var index = 0

    override fun indexOf(element: E): Int {
        return map[element]!!
    }

    override fun lastIndexOf(element: E): Int {
        return map[element]!!
    }

    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }

    override val size: Int
        get() {
            return map.size
        }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun contains(element: E): Boolean {
        return map.containsKey(element)
    }

    override fun iterator(): MutableIterator<E> {
        return map.keys.iterator()
    }

    override fun add(element: E): Boolean {
        if (!map.containsKey(element)) {
            val index = index++
            map[element] = index
            inverse[index] = element
            return true
        }
        return false
    }

    override fun remove(element: E): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return map.keys.containsAll(elements)
    }

    override fun addAll(c: Collection<E>): Boolean {
        for (e in c) {
            this.add(e)
        }
        return true
    }

    override fun addAll(index: Int, c: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun get(index: Int): E {
        return inverse[index]!!
    }

    override fun set(index: Int, element: E): E {
        throw UnsupportedOperationException()
    }

    override fun add(index: Int, element: E) {
        throw UnsupportedOperationException()
    }

    override fun removeAt(index: Int): E {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return map.keys.toString()
    }
}
