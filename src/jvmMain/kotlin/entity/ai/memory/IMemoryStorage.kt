package org.chorus_oss.chorus.entity.ai.memory

import org.chorus_oss.chorus.entity.Entity

/**
 * 记忆存储器
 *
 *
 * memory storage
 */
interface IMemoryStorage {
    /**
     * Write data to MemoryType
     *
     * @param type
     * @param data
     * @param <D>  数据类型
    </D> */
    operator fun <D> set(type: IMemoryType<D>, data: D)

    /**
     * 从指定记忆类型获取数据
     *
     *
     * Get data from the specified MemoryType
     *
     * @param type 记忆类型
     * @param <D>  数据类型
     * @return 数据
    </D> */
    operator fun <D : Any> get(type: NullableMemoryType<D>): D?

    operator fun <D : Any> get(type: MemoryType<D>): D

    /**
     * 获取所有记忆
     *
     *
     * get all memories
     *
     * @return 所有记忆
     */
    val all: Map<IMemoryType<*>, Any>

    /**
     * 清空指定记忆类型数据为null
     *
     *
     * Clear the specified MemoryType data to null
     *
     * @param type 记忆类型
     */
    fun clear(type: IMemoryType<*>)

    /**
     * 获取记忆存储所属的实体
     *
     *
     * Get the entity that the memory store belongs to
     *
     * @return 实体
     */
    val entity: Entity

    /**
     * 检查指定记忆类型数据是否为空(null)
     *
     *
     * Check if the specified memory type data is empty (null)
     *
     * @param type 记忆类型
     * @return 是否为空
     */
    fun isEmpty(type: NullableMemoryType<*>): Boolean {
        return get(type) == null
    }

    /**
     * 检查指定记忆类型数据是否不为空(null)
     *
     *
     * Check if the specified memory type data is not empty (null)
     *
     * @param type 记忆类型
     * @return 是否不为空
     */
    fun notEmpty(type: NullableMemoryType<*>): Boolean {
        return get(type) != null
    }

    /**
     * 将此记忆存储器的数据编码进所属实体NBT(若MemoryType附加有编解码器)
     *
     *
     * Encode the data of this memory storage into the entity NBT (if there is a codec attached to the Memory Type)
     */
    fun encode() {
        val entity = entity
        for ((key, value) in all) {
            key.forceEncode(entity, value)
        }
    }
}
