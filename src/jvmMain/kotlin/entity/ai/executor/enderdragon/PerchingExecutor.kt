package org.chorus_oss.chorus.entity.ai.executor.enderdragon

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.item.EntityAreaEffectCloud
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

class PerchingExecutor : EntityControl, IBehaviorExecutor {
    private var stayTick = -1

    override fun execute(entity: EntityMob): Boolean {
        val target = Vector3(0.0, (entity.level!!.getHighestBlockAt(0, 0) + 1).toDouble(), 0.0)
        if (stayTick >= 0) {
            stayTick++
        }
        if (entity.position.distance(target) <= 1) {
            if (stayTick == -1) stayTick = 0
            if (stayTick == 25) {
                entity.viewers.values.stream()
                    .filter { player: Player -> player.position.distance(Vector3(0.0, 64.0, 0.0)) <= 20 }.findAny()
                    .ifPresent { player: Player ->
                        removeRouteTarget(entity)
                        setLookTarget(entity, player.position)
                        val toPlayerVector = Vector3(
                            player.position.x - entity.position.x,
                            player.position.y - entity.position.y,
                            player.position.z - entity.position.z
                        ).normalize()
                        val transform = entity.transform.add(toPlayerVector.multiply(10.0))
                        transform.position.y =
                            (transform.level.getHighestBlockAt(transform.position.toHorizontal()) + 1).toDouble()
                        val areaEffectCloud = Entity.Companion.createEntity(
                            EntityID.AREA_EFFECT_CLOUD, transform.chunk,
                            CompoundTag().putList(
                                "Pos", ListTag<FloatTag>()
                                    .add(FloatTag(transform.position.x))
                                    .add(FloatTag(transform.position.y))
                                    .add(FloatTag(transform.position.z))
                            )
                                .putList(
                                    "Rotation", ListTag<FloatTag>()
                                        .add(FloatTag(0f))
                                        .add(FloatTag(0f))
                                )
                                .putList(
                                    "Motion", ListTag<FloatTag>()
                                        .add(FloatTag(0f))
                                        .add(FloatTag(0f))
                                        .add(FloatTag(0f))
                                )
                                .putInt("Duration", 60)
                                .putFloat("InitialRadius", 6f)
                                .putFloat("Radius", 6f)
                                .putFloat("Height", 1f)
                                .putFloat("RadiusChangeOnPickup", 0f)
                                .putFloat("RadiusPerTick", 0f)
                        ) as EntityAreaEffectCloud

                        val effects = PotionType.get(PotionType.HARMING.id).getEffects(false)
                        for (effect in effects) {
                            if (effect != null && areaEffectCloud != null) {
                                areaEffectCloud.cloudEffects!!.add(effect.setVisible(false).setAmbient(false))
                                areaEffectCloud.spawnToAll()
                            }
                        }
                        areaEffectCloud.spawnToAll()
                    }
            }
        } else {
            setRouteTarget(entity, target)
            setLookTarget(entity, target)
        }
        if (stayTick > 100) {
            return false
        } else if (stayTick >= 0) {
            entity.teleport(target)
        }
        return true
    }


    override fun onStart(entity: EntityMob) {
        val player = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_PLAYER)!!
        setLookTarget(entity, player.position)
        setRouteTarget(entity, player.position)
        stayTick = -1
    }

    override fun onStop(entity: EntityMob) {
        entity.memoryStorage.set(CoreMemoryTypes.FORCE_PERCHING, false)
        entity.isEnablePitch = (false)
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }
}
