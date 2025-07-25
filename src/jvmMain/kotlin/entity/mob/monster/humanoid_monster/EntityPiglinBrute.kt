package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.block.BlockDoor
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.PiglinTransformExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.*
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.Function

class EntityPiglinBrute(chunk: IChunk?, nbt: CompoundTag?) : EntityPiglin(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.PIGLIN_BRUTE
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    PiglinTransformExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob -> entity.level!!.dimension != Level.DIMENSION_NETHER },
                        IBehaviorEvaluator { entity: EntityMob? -> !isImmobile() },
                        IBehaviorEvaluator { entity: EntityMob -> !entity.namedTag!!.getBoolean("IsImmuneToZombification") }
                    ), 12, 1),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_PIGLIN_ANGRY, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> isAngry() }), 10, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_PIGLIN_AMBIENT, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> !isAngry() }), 9, 1
                ),
                Behavior(
                    PiglinMeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.5f, 40, true, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    6,
                    1
                ),
                Behavior(
                    PiglinMeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.5f, 40, false, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    5,
                    1
                ),
                Behavior(
                    PiglinFleeFromTargetExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                        IBehaviorEvaluator { entity: EntityMob? -> !isBaby() }
                    ), 3, 1),
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
                NearestPlayerAngryPiglinSensor(),
                NearestEntitySensor(
                    EntityZombiePigman::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    8.0,
                    0.0
                ),
                BlockSensor(BlockDoor::class.java, CoreMemoryTypes.NEAREST_BLOCK, 2, 2, 20)
            ),
            setOf(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        super.initEntity()
        this.maxHealth = 50
        this.diffHandDamage = floatArrayOf(6f, 10f, 15f)
        setItemInHand(Item.get(ItemID.GOLDEN_AXE))
    }

    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier()) {
            EntityID.WITHER_SKELETON, EntityID.WITHER -> true
            else -> false
        }
    }

    override fun getOriginalName(): String {
        return "Piglin Brute"
    }
}
