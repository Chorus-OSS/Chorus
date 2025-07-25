package org.chorus_oss.chorus.nbt.tag

import java.util.*

class TreeMapCompoundTag : CompoundTag {
    constructor() : super(TreeMap<String, Tag<*>>())

    constructor(tags: Map<String, Tag<*>>) : super(TreeMap<String, Tag<*>>(tags))

    override val tags: MutableMap<String, Tag<*>> = TreeMap(super.tags)

    override fun parseValue(): MutableMap<String, Any> {
        val value: MutableMap<String, Any> = TreeMap()
        for ((key, value1) in tags) {
            value[key] = value1.parseValue()
        }
        return value
    }

    override fun copy(): TreeMapCompoundTag {
        val nbt = TreeMapCompoundTag()
        tags.forEach { (key, value) -> nbt.put(key, value.copy()) }
        return nbt
    }
}
