package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.executor.evocation.ColorConversionExecutor
import org.chorus_oss.chorus.entity.ai.executor.evocation.FangCircleExecutor
import org.chorus_oss.chorus.entity.ai.executor.evocation.FangLineExecutor
import org.chorus_oss.chorus.entity.ai.executor.evocation.VexSummonExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntitySheep
import org.chorus_oss.chorus.entity.mob.monster.EntityCreaking
import org.chorus_oss.chorus.entity.mob.monster.EntityVex
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

class EntityEvocationIllager(chunk: IChunk?, nbt: CompoundTag?) : EntityIllager(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.EVOCATION_ILLAGER
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_EVOCATION_ILLAGER_AMBIENT), RandomSoundEvaluator(), 10, 1),
                Behavior(
                    FleeFromTargetExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY, 0.5f, true, 9f), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY, 8.0),
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE }
                        )
                    ), 9, 1),
                Behavior(
                    FleeFromTargetExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.5f,
                        true,
                        11f
                    ),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET, 10.0),
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE }
                        )
                    ),
                    8,
                    1),
                Behavior(
                    ColorConversionExecutor(), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_CONVERSION, 100),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 40),
                        IBehaviorEvaluator { entity: EntityMob ->
                            for (entity1 in entity.level!!.getNearbyEntities(
                                entity.getBoundingBox().grow(16.0, 16.0, 16.0)
                            )) {
                                if (entity1 is EntitySheep) {
                                    if (entity1.getColor() == DyeColor.BLUE.woolData) {
                                        return@IBehaviorEvaluator true
                                    }
                                }
                            }
                            false
                        },
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE },
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.COLOR_CONVERSION }
                        ),
                        IBehaviorEvaluator { entity: EntityMob ->
                            entity.level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)
                        }
                    ), 7, 1),
                Behavior(
                    VexSummonExecutor(), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_SUMMON, 340),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 40),
                        IBehaviorEvaluator { entity: EntityMob ->
                            var count = 0
                            for (entity1 in entity.level!!.getNearbyEntities(
                                entity.getBoundingBox().grow(15.0, 15.0, 15.0)
                            )) {
                                if (entity1 is EntityVex) count++
                            }
                            count < 8
                        },
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE },
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.SUMMON }
                        )
                    ), 6, 1),
                Behavior(
                    FangCircleExecutor(), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_CAST, 100),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 40),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET, 3.0),
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE },
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.CAST_CIRLCE }
                        )
                    ), 5, 1),
                Behavior(
                    FangLineExecutor(), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_CAST, 100),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 40),
                        any(
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.NONE },
                            IBehaviorEvaluator { it.memoryStorage[CoreMemoryTypes.LAST_MAGIC] == Spell.CAST_LINE }
                        )
                    ), 4, 1),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET, 1), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob -> !entity.getDataFlag(EntityFlag.CASTING) }
                    ), 3, 1),
                Behavior(
                    DoNothingExecutor(),
                    { entity: EntityMob -> entity.getDataFlag(EntityFlag.CASTING) }, 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf(
                NearestTargetEntitySensor<Entity>(
                    0.0, 16.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function { entity ->
                        this.attackTarget(
                            entity
                        )
                    }),
                NearestTargetEntitySensor<Entity>(
                    0.0, 16.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                    Function { entity -> entity is EntityCreaking })
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 24
        super.initEntity()
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Evoker"
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getExperienceDrops(): Int {
        return 10
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(
            Item.get(ItemID.TOTEM_OF_UNDYING),
            Item.get(ItemID.EMERALD, 0, ThreadLocalRandom.current().nextInt(2))
        )
    }


    enum class Spell {
        NONE,
        CAST_LINE,
        CAST_CIRLCE,
        SUMMON,
        COLOR_CONVERSION
    }
}
