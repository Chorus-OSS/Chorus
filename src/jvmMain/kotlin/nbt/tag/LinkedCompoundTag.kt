package org.chorus_oss.chorus.nbt.tag

class LinkedCompoundTag @JvmOverloads constructor(tags: MutableMap<String, Tag<*>> = LinkedHashMap()) :
    CompoundTag(tags) {
    override val tags: MutableMap<String, Tag<*>> = LinkedHashMap(super.tags)

    override fun parseValue(): MutableMap<String, Any> {
        val value: MutableMap<String, Any> = LinkedHashMap(
            tags.size
        )

        for ((key, value1) in tags) {
            value[key] = value1.parseValue()
        }

        return value
    }

    override fun copy(): LinkedCompoundTag {
        val nbt = LinkedCompoundTag()
        tags.forEach { (key, value) -> nbt.put(key, value.copy()) }
        return nbt
    }
}
