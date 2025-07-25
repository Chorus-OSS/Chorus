package org.chorus_oss.chorus.entity.ai.memory

import org.chorus_oss.chorus.entity.mob.EntityMob

import java.util.concurrent.ConcurrentHashMap

class MemoryStorage(override var entity: EntityMob) : IMemoryStorage {
    private val memoryMap: MutableMap<IMemoryType<*>, Any> = ConcurrentHashMap()

    override fun <D> set(type: IMemoryType<D>, data: D) {
        type.codec?.init(data, entity)
        if (data != null) {
            memoryMap[type] = data
        } else memoryMap.remove(type)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <D : Any> get(type: NullableMemoryType<D>): D? {
        if (!memoryMap.containsKey(type)) {
            val data = type.decode(entity)
            if (data != null) {
                set(type, data)
            }
        }
        return memoryMap[type] as D?
    }

    @Suppress("UNCHECKED_CAST")
    override fun <D : Any> get(type: MemoryType<D>): D {
        if (!memoryMap.containsKey(type)) {
            val data = type.decode(entity) ?: type.getDefaultData()
            set(type, data)
        }
        return memoryMap[type] as D
    }

    override val all: Map<IMemoryType<*>, Any>
        get() = memoryMap

    override fun clear(type: IMemoryType<*>) {
        memoryMap.remove(type)
    }
}
