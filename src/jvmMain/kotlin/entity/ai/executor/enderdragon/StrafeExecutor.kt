package org.chorus_oss.chorus.entity.ai.executor.enderdragon

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import kotlin.math.cos
import kotlin.math.sin

class StrafeExecutor : EntityControl, IBehaviorExecutor {
    private var fired = false

    override fun execute(entity: EntityMob): Boolean {
        if (fired) return false

        val player = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_PLAYER)!!
        setLookTarget(entity, player.position)
        setRouteTarget(entity, player.position)

        if (entity.position.distance(player.position) <= 64) {
            val toPlayerVector = Vector3(
                player.position.x - entity.position.x,
                player.position.y - entity.position.y,
                player.position.z - entity.position.z
            ).normalize()

            val fireballTransform = entity.transform.add(toPlayerVector.multiply(5.0))
            val yaw = BVector3.getYawFromVector(toPlayerVector)
            val pitch = BVector3.getPitchFromVector(toPlayerVector)
            val nbt = CompoundTag()
                .putList(
                    "Pos", ListTag<FloatTag>()
                        .add(FloatTag(fireballTransform.position.x))
                        .add(FloatTag(fireballTransform.position.y))
                        .add(FloatTag(fireballTransform.position.z))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(-sin(yaw / 180 * Math.PI) * cos(pitch / 180 * Math.PI)))
                        .add(FloatTag(-sin(pitch / 180 * Math.PI)))
                        .add(FloatTag(cos(yaw / 180 * Math.PI) * cos(pitch / 180 * Math.PI)))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )

            val projectile = Entity.createEntity(
                EntityID.DRAGON_FIREBALL,
                entity.level!!.getChunk(entity.position.chunkX, entity.position.chunkZ),
                nbt
            )
            projectile?.spawnToAll()
            this.fired = true
            return false
        }
        return true
    }


    override fun onStart(entity: EntityMob) {
        val player = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_PLAYER)!!
        setLookTarget(entity, player.position)
        setRouteTarget(entity, player.position)
        this.fired = false
    }

    override fun onStop(entity: EntityMob) {
        entity.isEnablePitch = (false)
        entity.memoryStorage.clear(CoreMemoryTypes.LAST_ENDER_CRYSTAL_DESTROY)
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }
}
