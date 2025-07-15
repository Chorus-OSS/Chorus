package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes

/**
 * 实体可剪切
 *
 *
 * 例如羊就可被剪羊毛
 *
 *
 * 若作用于此实体的物品的isShears()为true，则将会调用此方法
 * <br></br>
 * Entities that can be sheared. Stores value with [CoreMemoryTypes.IS_SHEARED]
 */
interface EntityShearable : EntityComponent {
    /**
     * @return 此次操作是否有效。若有效，将会减少物品耐久 true if shearing succeeded.
     */
    fun shear(): Boolean {
        val entity = asEntity()
        if (this.isSheared() || (entity is EntityAgeable && entity.isBaby())) {
            return false
        }
        this.setIsSheared(true)
        return true
    }

    fun isSheared(): Boolean {
        return memoryStorage.get<Boolean>(CoreMemoryTypes.IS_SHEARED)
    }

    fun setIsSheared(isSheared: Boolean) {
        memoryStorage.set<Boolean>(CoreMemoryTypes.IS_SHEARED, isSheared)
    }
}
