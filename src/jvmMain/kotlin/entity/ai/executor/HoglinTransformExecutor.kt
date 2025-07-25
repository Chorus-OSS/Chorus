package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.EntityZoglin
import org.chorus_oss.chorus.level.Sound

class HoglinTransformExecutor : EntityControl, IBehaviorExecutor {
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
        val zoglin = EntityZoglin(entity.locator.chunk, entity.namedTag)
        zoglin.setPosition(entity.position)
        zoglin.setRotation(entity.rotation.yaw, entity.rotation.pitch)
        zoglin.setBaby(entity.isBaby())
        zoglin.spawnToAll()
        zoglin.level!!.addSound(zoglin.position, Sound.MOB_HOGLIN_CONVERTED_TO_ZOMBIFIED)
        zoglin.addEffect(Effect.get(EffectType.NAUSEA).setDuration(15))
    }
}


