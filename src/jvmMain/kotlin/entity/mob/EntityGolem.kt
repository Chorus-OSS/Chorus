package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.monster.EntityCreeper
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityGolem(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityWalkable, EntityCanAttack {
    override fun attack(source: EntityDamageEvent): Boolean {
        if (source is EntityDamageByEntityEvent && source.damager !is EntityCreeper) {
            memoryStorage[CoreMemoryTypes.ATTACK_TARGET] = source.damager
        }
        return super.attack(source)
    }

    override fun attackTarget(entity: Entity): Boolean {
        if (entity is EntityGolem) return false
        if (entity is EntityAllay) return false
        return entity is EntityMonster
    }
}
