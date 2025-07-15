package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockTurtleEgg
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.BlockSensor
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import java.util.function.Function

class EntityHusk(chunk: IChunk?, nbt: CompoundTag?) : EntityZombie(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.HUSK
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    PlaySoundExecutor(
                        Sound.MOB_HUSK_AMBIENT,
                        if (isBaby()) 1.3f else 0.8f,
                        if (isBaby()) 1.7f else 1.2f,
                        1f,
                        1f
                    ), RandomSoundEvaluator(), 7, 1
                ),
                Behavior(
                    JumpExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob? -> !memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_BLOCK) },
                        IBehaviorEvaluator { entity: EntityMob ->
                            entity.getCollisionBlocks()!!.stream().anyMatch { block: Block? -> block is BlockTurtleEgg }
                        }), 6, 1, 10
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_BLOCK, 0.3f, true),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK),
                    5,
                    1
                ),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.ATTACK_TARGET, 0.3f, 40, true, 10, Effect.get(
                            EffectType.HUNGER
                        ).setDuration(140)
                    ), EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET), 4, 1
                ),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 40, false, 10, Effect.get(
                            EffectType.HUNGER
                        ).setDuration(140)
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER), 3, 1
                ),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.3f,
                        40,
                        true,
                        30,
                        Effect.get(
                            EffectType.HUNGER
                        ).setDuration(140)
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET), 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                NearestPlayerSensor(40.0, 0.0, 20),
                NearestTargetEntitySensor<Entity>(
                    0.0, 16.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function<Entity, Boolean> { entity: Entity? ->
                        this.attackTarget(
                            entity!!
                        )
                    }),
                BlockSensor(BlockTurtleEgg::class.java, CoreMemoryTypes.NEAREST_BLOCK, 11, 15, 10)

            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 20
        this.diffHandDamage = floatArrayOf(2.5f, 3f, 4.5f)
        super.initEntity()
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_INTERVAL, 8)
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_INTERVAL_RANGE, 16)
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_EVENT_NAME, LevelSoundEventPacket.SOUND_AMBIENT)
        if (this.isBaby()) {
            this.setDataProperty(
                EntityDataTypes.AMBIENT_SOUND_EVENT_NAME,
                LevelSoundEventPacket.SOUND_AMBIENT_BABY
            )
        }
    }

    override fun getWidth(): Float {
        return if (this.isBaby()) 0.3f else 0.6f
    }

    override fun getHeight(): Float {
        return if (this.isBaby()) 0.95f else 1.9f
    }

    override fun getOriginalName(): String {
        return "Husk"
    }

    override fun isUndead(): Boolean {
        return true
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getFloatingForceFactor(): Double {
        return 0.0
    }

    override fun transform() {
        this.close()
        val drowned = EntityZombie(this.locator.chunk, this.namedTag)
        drowned.setPosition(this.position)
        drowned.setRotation(rotation.yaw, rotation.pitch)
        drowned.spawnToAll()
        drowned.level!!.addSound(drowned.position, Sound.MOB_HUSK_CONVERT_TO_ZOMBIE)
    }
}
