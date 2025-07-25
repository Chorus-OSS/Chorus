package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage

/**
 * 继承了此接口的接口为一个实体组件<br></br>
 * 实体组件的实现使用default方法承载逻辑，相关值则使用记忆存储器存储
 *
 *
 * The interface that inherits this interface is an entity component<br></br>
 * The implementation of the entity component uses the default method to carry the logic, and the related values are stored in memory
 */
interface EntityComponent {
    val memoryStorage: IMemoryStorage

    fun asEntity(): Entity? {
        return memoryStorage.entity
    }
}
