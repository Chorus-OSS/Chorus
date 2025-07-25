package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityWolf

/**
 * 狼执行攻击，会带有狼的动画，以及攻击过程中狼还会看向携带食物的玩家.
 *
 *
 * The wolf performs an attack with a wolf animation, as well as during the attack the wolf will also look at the player carrying food.
 */
class WolfAttackExecutor
/**
 * 近战攻击执行器
 *
 * @param memory            记忆
 * @param speed             移动向攻击目标的速度
 * @param maxSenseRange     最大获取攻击目标范围
 * @param clearDataWhenLose 失去目标时清空记忆
 * @param coolDown          攻击冷却时间(单位tick)
 */(
    memory: NullableMemoryType<out Entity>,
    speed: Float,
    maxSenseRange: Int,
    clearDataWhenLose: Boolean,
    coolDown: Int
) :
    MeleeAttackExecutor(memory, speed, maxSenseRange, clearDataWhenLose, coolDown) {
    override fun execute(entity: EntityMob): Boolean {
        val wolf = entity as EntityWolf

        //        target = entity.getBehaviorGroup().memoryStorage.get(memory);
//        if ((target != null && !target.isAlive()) || (target != null && target.equals(entity))) return false;
        wolf.setAngry(true)

        if (entity.memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            if (!entity.isEnablePitch) entity.isEnablePitch = (true)
            val vector3 = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)
            if (vector3 != null) {
                this.lookTarget = vector3.position.clone()
                entity.setDataFlag(EntityFlag.INTERESTED, true)
            }
        }
        return super.execute(entity)
    }

    override fun onStop(entity: EntityMob) {
        stop(entity)
        super.onStop(entity)
    }

    override fun onInterrupt(entity: EntityMob) {
        stop(entity)
        super.onInterrupt(entity)
    }

    private fun stop(entity: EntityMob) {
        val wolf = entity as EntityWolf
        entity.level!!.scheduler.scheduleDelayedTask(null, { wolf.setAngry(false) }, 5)

        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false)
        }
    }
}
