package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.EntityOwnable
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob

class EntityAttackedByOwnerSensor(override var period: Int, protected var changeTarget: Boolean) : ISensor {
    override fun sense(entity: EntityMob) {
        if (entity is EntityOwnable) {
            val player = entity.owner
            if (player != null) {
                val current = entity.memoryStorage[CoreMemoryTypes.ENTITY_ATTACKED_BY_OWNER]
                if (!changeTarget) {
                    if (current != null && current.isAlive()) return
                    else entity.memoryStorage.clear(CoreMemoryTypes.ENTITY_ATTACKED_BY_OWNER)
                }
                if (player.lastBeAttackEntity != null) {
                    entity.memoryStorage[CoreMemoryTypes.ENTITY_ATTACKING_OWNER] = player.lastBeAttackEntity
                } else if (player.lastAttackEntity != null) {
                    entity.memoryStorage[CoreMemoryTypes.ENTITY_ATTACKED_BY_OWNER] = player.lastAttackEntity
                } else entity.memoryStorage.clear(CoreMemoryTypes.ENTITY_ATTACKED_BY_OWNER)
            }
        }
    }
}
