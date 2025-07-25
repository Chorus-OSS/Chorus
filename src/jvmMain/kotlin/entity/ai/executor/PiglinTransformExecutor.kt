package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityZombiePigman
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.level.Sound

class PiglinTransformExecutor : EntityControl, IBehaviorExecutor {
    protected var tick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        tick++
        if (tick >= 300) {
            transform(entity)
            return false
        }
        return true
    }


    override fun onStart(entity: EntityMob) {
        tick = -1
        entity.setDataFlag(EntityFlag.SHAKING)
    }

    override fun onStop(entity: EntityMob) {
        entity.setDataFlag(EntityFlag.SHAKING, false)
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }

    private fun transform(entity: EntityMob) {
        entity.saveNBT()
        entity.close()
        val entityZombiePigman = EntityZombiePigman(entity.locator.chunk, entity.namedTag)
        entityZombiePigman.setPosition(entity.position)
        entityZombiePigman.setRotation(entity.rotation.yaw, entity.rotation.pitch)
        entityZombiePigman.spawnToAll()
        entityZombiePigman.level!!.addSound(entityZombiePigman.position, Sound.MOB_PIGLIN_CONVERTED_TO_ZOMBIFIED)
        val inventory: Inventory = entityZombiePigman.equipment
        for (i in 2..<inventory.size) {
            entityZombiePigman.level!!.dropItem(entityZombiePigman.position, inventory.getItem(i))
            inventory.clear(i)
        }
        // TODO
        entityZombiePigman.addEffect(Effect.get(EffectType.NAUSEA).setDuration(15))
    }
}


