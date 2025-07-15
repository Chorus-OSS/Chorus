package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntitySmite
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.BowShootExecutor
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityBogged(chunk: IChunk?, nbt: CompoundTag?) : EntitySkeleton(chunk, nbt), EntityWalkable, EntitySmite {
    override fun getEntityIdentifier(): String {
        return EntityID.BOGGED
    }

    override fun initEntity() {
        this.maxHealth = 16
        super.initEntity()
        if (itemInHand.isNothing) {
            setItemInHand(Item.get(ItemID.BOW))
        }
    }

    override fun getOriginalName(): String {
        return "Bogged"
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(Item.get(ItemID.BONE), Item.get(ItemID.ARROW)) // TODO: match vanilla drop
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_SKELETON_SAY), RandomSoundEvaluator(), 4, 1),
                Behavior(
                    BowShootExecutor(
                        { this.itemInHand },
                        CoreMemoryTypes.ATTACK_TARGET,
                        0.3f,
                        15,
                        true,
                        35,
                        20
                    ), EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET), 3, 1
                ),
                Behavior(
                    BowShootExecutor(
                        { this.itemInHand },
                        CoreMemoryTypes.NEAREST_PLAYER,
                        0.3f,
                        15,
                        true,
                        35,
                        20
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER), 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(NearestPlayerSensor(16.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }
}
