package org.chorus_oss.chorus.entity.mob.water_animal.fish

import org.chorus_oss.chorus.entity.EntitySwimmable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.DiveController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.SpaceMoveController
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.SwimmingPosEvaluator
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.water_animal.EntityWaterAnimal
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

/**
 * 所有鱼的基类
 */
abstract class EntityFish(chunk: IChunk?, nbt: CompoundTag) : EntityWaterAnimal(chunk, nbt), EntitySwimmable {
    //移除搁浅音效很不对味
    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf(),
            setOf<IBehavior>(
                Behavior(
                    SpaceRandomRoamExecutor(0.36f, 12, 1, 80, false, -1, false, 10),
                    { entity: EntityMob? -> true }, 1
                )
            ),
            setOf(),
            setOf(SpaceMoveController(), LookController(true, true), DiveController()),
            SimpleSpaceAStarRouteFinder(SwimmingPosEvaluator(), this),
            this
        )
    }
}
