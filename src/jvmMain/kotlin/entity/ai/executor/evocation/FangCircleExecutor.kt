package org.chorus_oss.chorus.entity.ai.executor.evocation

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager.Spell
import org.chorus_oss.chorus.level.Transform
import kotlin.math.cos
import kotlin.math.sin

class FangCircleExecutor : FangLineExecutor() {
    override fun execute(entity: EntityMob): Boolean {
        val illager = entity as EntityEvocationIllager
        if (tick == CAST_DURATION) {
            entity.rotation.yaw = (entity.headYaw)
            summon(illager, SPAWN_RADIUS_INNER, SPAWN_COUNT_INNER)
        } else if (tick == CAST_DURATION + DELAY_BETWEEN) {
            summon(illager, SPAWN_RADIUS_OUTER, SPAWN_COUNT_OUTER)
        }
        tick++
        if (tick >= DURATION) {
            val tick = entity.level!!.tick
            entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_CAST, tick)
            entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_TIME, tick)
            return false
        } else return true
    }

    private fun summon(origin: EntityEvocationIllager, size: Float, amount: Int) {
        val angleIncrement = 360.0 / amount
        for (i in 0..<amount) {
            val angle = Math.toRadians((i * angleIncrement) + origin.headYaw)
            val particleX = origin.vector3.x + cos(angle) * size
            val particleZ = origin.vector3.z + sin(angle) * size
            spawn(origin, Transform(particleX, origin.position.y, particleZ, angle, 0.0, origin.level!!))
        }
    }

    override fun startSpell(entity: EntityMob) {
        super.startSpell(entity)
        entity.memoryStorage.set(CoreMemoryTypes.LAST_MAGIC, Spell.CAST_CIRLCE)
    }

    companion object {
        //Values represent ticks
        private const val CAST_DURATION = 40
        private const val DELAY_BETWEEN = 3
        private const val SPAWN_COUNT_INNER = 5
        private const val SPAWN_COUNT_OUTER = 8
        private const val SPAWN_RADIUS_INNER = 1.5f
        private const val SPAWN_RADIUS_OUTER = 2.5f


        private const val DURATION = CAST_DURATION + DELAY_BETWEEN + 1
    }
}
