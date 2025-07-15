package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityRavager(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.RAVAGER
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.2f, 40, true, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    3,
                    1
                ),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.2f, 40, false, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    2,
                    1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(NearestPlayerSensor(40.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 100
        this.diffHandDamage = floatArrayOf(7f, 12f, 18f)
        super.initEntity()
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getWidth(): Float {
        return 1.2f
    }

    override fun getOriginalName(): String {
        return "Ravager"
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getExperienceDrops(): Int {
        return 20
    }
}
