package org.chorus_oss.chorus.entity.mob.water_animal

import org.chorus_oss.chorus.entity.EntityID
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
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityTadpole(chunk: IChunk?, nbt: CompoundTag) : EntityWaterAnimal(chunk, nbt), EntitySwimmable {
    override fun getEntityIdentifier(): String {
        return EntityID.TADPOLE
    }

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

    override fun getHeight(): Float {
        return 0.8f
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun initEntity() {
        this.maxHealth = 6
        super.initEntity()
    }

    override fun getOriginalName(): String {
        return "Tadpole"
    }
}
