package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityFlyable
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LiftController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.SpaceMoveController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.GhastShootExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.FlyingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.projectile.EntityFireball
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils

class EntityGhast(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityFlyable {
    override fun getEntityIdentifier(): String {
        return EntityID.GHAST
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_GHAST_MOAN), RandomSoundEvaluator(), 2, 1),
                Behavior(SpaceRandomRoamExecutor(0.15f, 12, 100, 20, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<IBehavior>(
                Behavior(
                    GhastShootExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 64, true, 60, 10),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    2,
                    1
                ),
                Behavior(
                    GhastShootExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 28, true, 60, 10),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    1,
                    1
                )
            ),
            setOf<ISensor>(NearestPlayerSensor(64.0, 0.0, 20)),
            setOf<IController>(SpaceMoveController(), LookController(true, true), LiftController()),
            SimpleSpaceAStarRouteFinder(FlyingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 10
        super.initEntity()
    }

    override fun kill() {
        level!!.entities.values.filter { entity: Entity ->
            if (entity is EntityFireball) {
                return@filter entity.shootingEntity === this && !entity.closed
            }
            false
        }.forEach { obj: Entity -> obj.close() }
        super.kill()
    }

    override fun getWidth(): Float {
        return 4f
    }

    override fun getHeight(): Float {
        return 4f
    }

    override fun getOriginalName(): String {
        return "Ghast"
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(
            Item.get(ItemID.GHAST_TEAR, 0, Utils.rand(0, 1)),
            Item.get(ItemID.GUNPOWDER, 0, Utils.rand(0, 2))
        )
    }

    override fun getExperienceDrops(): Int {
        return 5
    }
}
