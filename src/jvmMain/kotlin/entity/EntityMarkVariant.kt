package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.utils.Utils

/**
 * 实现这个接口的实体拥有次要变种属性
 */
interface EntityMarkVariant : EntityComponent {
    fun getMarkVariant(): Int {
        return memoryStorage[CoreMemoryTypes.MARK_VARIANT]!!
    }

    fun setMarkVariant(variant: Int) {
        memoryStorage.set(CoreMemoryTypes.MARK_VARIANT, variant)
    }

    fun hasMarkVariant(): Boolean {
        return memoryStorage.notEmpty(CoreMemoryTypes.MARK_VARIANT)
    }

    /**
     * 随机一个变种值
     */
    fun randomMarkVariant(): Int {
        return getAllMarkVariant()[Utils.rand(0, getAllMarkVariant().size - 1)]
    }

    /**
     * 定义全部可能的变种
     */
    fun getAllMarkVariant(): IntArray
}
