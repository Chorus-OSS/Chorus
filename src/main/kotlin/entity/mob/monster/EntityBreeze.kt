package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.DistanceEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils

class EntityBreeze(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.BREEZE
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    BreezeShootExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.4f, 15, true, 30, 20),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    2,
                    1
                ),
                Behavior(
                    BreezeShootExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.4f, 15, true, 30, 20),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    2,
                    1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    PlaySoundExecutor(Sound.MOB_BREEZE_IDLE_AIR), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> !isOnGround() }), 7, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_BREEZE_IDLE_GROUND), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> isOnGround() }), 6, 1
                ),
                Behavior(
                    BreezeJumpExecutor(), all(
                        any(
                            IBehaviorEvaluator { obj: EntityMob -> obj.isOnGround() },
                            IBehaviorEvaluator { obj: EntityMob -> obj.isInsideOfWater() }),
                        IBehaviorEvaluator { entity: EntityMob? -> getRiding() == null },
                        IBehaviorEvaluator { entity: EntityMob? -> !isInsideOfLava() }), 5, 1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.ATTACK_TARGET, 1.2f, true),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        DistanceEvaluator(CoreMemoryTypes.ATTACK_TARGET, 24.0)
                    ),
                    4,
                    1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 1.2f, true),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_PLAYER, 24.0)
                    ),
                    3,
                    1
                ),
                Behavior(FlatRandomRoamExecutor(1f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(NearestPlayerSensor(24.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 30
        super.initEntity()
    }

    override fun getHeight(): Float {
        return 1.77f
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(Item.get(ItemID.BREEZE_ROD, 0, Utils.rand(1, 2)))
    }

    override fun getExperienceDrops(): Int {
        return 10
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (source.cause == DamageCause.FALL) {
            return false
        }
        return super.attack(source)
    }
}
