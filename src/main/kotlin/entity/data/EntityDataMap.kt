package org.chorus_oss.chorus.entity.data

import com.google.common.base.Preconditions
import java.util.*

class EntityDataMap : MutableMap<EntityDataType<Any>, Any> {
    private val map: MutableMap<EntityDataType<Any>, Any> = LinkedHashMap()

    fun getOrCreateFlags(): EnumSet<EntityFlag> {
        var flags = get(EntityDataTypes.FLAGS) as EnumSet<EntityFlag>?
        if (flags == null) {
            flags = get(EntityDataTypes.FLAGS_2) as EnumSet<EntityFlag>?
            if (flags == null) {
                flags = EnumSet.noneOf(EntityFlag::class.java)
            }
            this.putFlags(flags!!)
        }
        return flags
    }

    fun getFlags(): EnumSet<EntityFlag> {
        return getType<EnumSet<EntityFlag>>(EntityDataTypes.FLAGS)
    }

    fun setFlag(flag: EntityFlag, value: Boolean): EntityFlag {
        val flags: EnumSet<EntityFlag> = this.getOrCreateFlags()
        if (value) {
            flags.add(flag)
        } else {
            flags.remove(flag)
        }

        return flag
    }

    fun existFlag(flag: EntityFlag): Boolean {
        val flags: EnumSet<EntityFlag> = this.getOrCreateFlags()
        return flags.contains(flag)
    }

    fun putFlags(flags: EnumSet<EntityFlag>): EnumSet<EntityFlag> {
        map[EntityDataTypes.FLAGS] = flags
        map[EntityDataTypes.FLAGS_2] = flags
        return flags
    }

    fun <T : Any> getType(type: EntityDataType<T>): T {
        return map.getOrDefault(type, type.getDefaultValue()) as T
    }

    fun <T : Any> getOrDefault(type: EntityDataType<T>, defaultValue: T): T {
        val `object`: Any = map.getOrDefault(type, defaultValue)
        return try {
            `object` as T
        } catch (e: ClassCastException) {
            defaultValue
        }
    }

    fun <T : Any> putType(type: EntityDataType<T>, value: T) {
        this[type] = value
    }

    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun containsKey(key: EntityDataType<*>): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: Any): Boolean {
        return map.containsValue(value)
    }

    override fun get(key: EntityDataType<*>): Any? {
        return map[key]
    }

    override fun put(key: EntityDataType<*>, value: Any): Any? {
        var value1: Any = value
        if (key === EntityDataTypes.FLAGS || key === EntityDataTypes.FLAGS_2) {
            return this.putFlags(value1 as EnumSet<EntityFlag>)
        }
        if (Number::class.java.isAssignableFrom(value1.javaClass)) {
            val type = key.getType()
            val number: Number = value1 as Number
            when (type) {
                Long::class -> value1 = number.toLong()
                Int::class -> value1 = number.toInt()
                Short::class -> value1 = number.toShort()
                Byte::class -> value1 = number.toByte()
                Float::class -> value1 = number.toFloat()
                Double::class -> value1 = number.toDouble()
            }
        }
        return map.put(key, value1)
    }

    override fun remove(key: EntityDataType<*>): Any? {
        return map.remove(key)
    }

    override fun putAll(from: Map<out EntityDataType<*>, Any>) {
        this.map.putAll(map)
    }

    override fun clear() {
        map.clear()
    }

    override val keys: MutableSet<EntityDataType<*>>
        get() = map.keys

    override val values: MutableCollection<Any>
        get() = map.values

    override val entries: MutableSet<MutableMap.MutableEntry<EntityDataType<*>, Any>>
        get() = map.entries

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that: EntityDataMap = other as EntityDataMap
        return this.map == that.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    fun copy(vararg entityDataTypes: EntityDataType<*>): EntityDataMap {
        val entityDataMap = EntityDataMap()
        for (t: EntityDataType<*> in entityDataTypes) {
            val o: Any? = this[t]
            if (o != null) {
                entityDataMap[t] = o
            }
        }
        return entityDataMap
    }

    override fun toString(): String {
        val i: Iterator<Map.Entry<EntityDataType<*>, Any>> = map.entries.iterator()
        if (!i.hasNext()) return "{}"

        val sb: StringBuilder = StringBuilder()
        sb.append('{')
        while (i.hasNext()) {
            val e: Map.Entry<EntityDataType<*>, Any> = i.next()
            val key: EntityDataType<*> = e.key
            if (key === EntityDataTypes.FLAGS_2) continue  // We don't want this to be visible.

            val stringVal: String = e.value.toString()
            sb.append(key.toString()).append('=').append(stringVal)
            if (!i.hasNext()) return sb.append('}').toString()
            sb.append(',').append(' ')
        }
        return sb.toString()
    }
}
