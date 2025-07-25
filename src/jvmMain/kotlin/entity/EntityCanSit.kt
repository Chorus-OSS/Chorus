package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag

/**
 * 可坐下实体接口
 *
 *
 */
interface EntityCanSit : EntityComponent {
    fun isSitting(): Boolean {
        return memoryStorage[CoreMemoryTypes.IS_SITTING]
    }

    fun setSitting(sitting: Boolean) {
        memoryStorage.set(CoreMemoryTypes.IS_SITTING, sitting)
        asEntity()!!.setDataFlag(EntityFlag.SITTING, sitting)
    }
}
