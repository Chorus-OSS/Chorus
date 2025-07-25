package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.utils.Utils

/**
 * 实现这个接口的实体拥有变种属性
 */
interface EntityVariant : EntityComponent {
    var variant: Int
        get() = memoryStorage[CoreMemoryTypes.VARIANT]!!
        set(value) {
            memoryStorage[CoreMemoryTypes.VARIANT] = value
        }

    fun hasVariant(): Boolean {
        return memoryStorage.notEmpty(CoreMemoryTypes.VARIANT)
    }

    /**
     * 随机一个变种值
     */
    fun randomVariant(): Int {
        return getAllVariant()[Utils.rand(0, getAllVariant().size - 1)]
    }

    /**
     * 定义全部可能的变种
     */
    fun getAllVariant(): IntArray
}
