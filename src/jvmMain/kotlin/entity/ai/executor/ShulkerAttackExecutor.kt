package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.EntityShulker
import org.chorus_oss.chorus.entity.projectile.EntityShulkerBullet
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Utils

class ShulkerAttackExecutor(
    protected val target: NullableMemoryType<out Entity>
) : IBehaviorExecutor {

    private var tick = 0
    private var nextAttack = 0

    override fun execute(entity: EntityMob): Boolean {
        val target = entity.memoryStorage[target] ?: return false
        tick++
        if (tick > nextAttack) {
            tick = 0
            nextAttack = Utils.rand(20, 110)
            val bulletTransform = entity.transform.clone().add(
                Vector3(
                    target.position.x - entity.position.x,
                    target.position.y - entity.position.y,
                    target.position.z - entity.position.z
                ).normalize()
            ).add(0.0, 0.5, 0.0)
            val nbt = CompoundTag()
                .putList(
                    "Pos", ListTag<FloatTag>()
                        .add(FloatTag(bulletTransform.position.x))
                        .add(FloatTag(bulletTransform.position.y))
                        .add(FloatTag(bulletTransform.position.z))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )


            val bulletEntity = Entity.createEntity(
                EntityID.SHULKER_BULLET,
                entity.level!!.getChunk(entity.position.chunkX, entity.position.chunkZ),
                nbt
            )

            if (bulletEntity is EntityShulkerBullet) {
                bulletEntity.memoryStorage.set(CoreMemoryTypes.ATTACK_TARGET, target)
            }
            bulletEntity?.spawnToAll()
            entity.level!!.addSound(entity.position, Sound.MOB_SHULKER_SHOOT)
        }
        return AllMatchEvaluator(
            EntityCheckEvaluator(this.target),
            DistanceEvaluator(this.target, 16.0),
            NotMatchEvaluator(PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, 0, 60))
        ).evaluate(entity)
    }

    override fun onStart(entity: EntityMob) {
        tick = 0
        nextAttack = 0
        if (entity is EntityShulker) {
            entity.setPeeking(40)
            val target = entity.memoryStorage[target] ?: return
            entity.setDataProperty(EntityDataTypes.TARGET_EID, target.getRuntimeID())
        }
    }

    override fun onStop(entity: EntityMob) {
        if (entity is EntityShulker) {
            entity.setDataProperty(EntityDataTypes.TARGET_EID, 0)
            entity.setPeeking(0)
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }
}
