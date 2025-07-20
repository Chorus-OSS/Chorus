package org.chorus_oss.chorus.level.updater.util.tagupdater

import java.util.*

class CompoundTagEditHelper(var rootTag: MutableMap<String, Any?>) {
    private val parentTag: Deque<MutableMap<String, Any?>> = ArrayDeque()
    private val tagName: Deque<String?> = ArrayDeque()
    var tag: Any?
        private set

    init {
        this.tag = rootTag
    }

    val compoundTag: MutableMap<String, Any?>
        get() = tag as MutableMap<String, Any?>

    val parent: Map<String, Any>?
        get() {
            if (!parentTag.isEmpty()) {
                val tag = parentTag.peekLast()
                if (tag is Map<*, *>) {
                    return tag as Map<String, Any>
                }
            }
            return null
        }

    fun canPopChild(): Boolean {
        return !parentTag.isEmpty()
    }

    fun popChild() {
        if (!parentTag.isEmpty()) {
            this.tag = parentTag.pollLast()
            tagName.pollLast()
        }
    }

    fun pushChild(name: String?) {
        requireNotNull(name) { "name" }
        check(tag is Map<*, *>) { "Tag is not of Compound type" }
        parentTag.addLast(this.tag as MutableMap<String, Any?>?)
        tagName.addLast(name)
        this.tag = (tag as MutableMap<String, Any?>)[name]
    }

    fun replaceWith(name: String, newTag: Any) {
        this.tag = newTag
        if (parentTag.isEmpty()) {
            this.rootTag = tag as MutableMap<String, Any?>
            return
        }
        val tag = parentTag.last
        if (tag is Map<*, *>) {
            val map = tag as MutableMap<String?, Any?>
            map.remove(tagName.pollLast())
            map[name] = newTag
            tagName.offerLast(name)
        }
    }
}
