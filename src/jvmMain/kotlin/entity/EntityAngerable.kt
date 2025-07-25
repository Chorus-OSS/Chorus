package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag

interface EntityAngerable : EntityComponent {
    fun isAngry(): Boolean {
        return memoryStorage[CoreMemoryTypes.IS_ANGRY]
    }

    fun setAngry(angry: Boolean) {
        memoryStorage.set(CoreMemoryTypes.IS_ANGRY, angry)
        asEntity()!!.setDataFlag(EntityFlag.ANGRY, angry)
    }
}
