package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

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
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.executor.PotionThrowExecutor
import org.chorus_oss.chorus.entity.ai.executor.UsePotionExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.mob.EntityGolem
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

class EntityWitch(chunk: IChunk?, nbt: CompoundTag?) : EntityHumanoidMonster(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.WITCH
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_WITCH_AMBIENT), RandomSoundEvaluator(), 2, 1),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<IBehavior>(
                Behavior(
                    UsePotionExecutor(0.3f, 30, 20), all(
                        IBehaviorEvaluator { entity: EntityMob ->
                            entity.level!!.tick - memoryStorage.get<Int>(
                                CoreMemoryTypes.LAST_BE_ATTACKED_TIME
                            ) <= 1
                        }
                    ), 4, 1),
                Behavior(
                    PotionThrowExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 15, true, 30, 20),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    3,
                    1
                ),
                Behavior(
                    PotionThrowExecutor(CoreMemoryTypes.NEAREST_GOLEM, 0.3f, 15, true, 30, 20),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_GOLEM),
                    2,
                    1
                ),
                Behavior(
                    PotionThrowExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 15, true, 30, 20),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    1,
                    1
                )
            ),
            setOf<ISensor>(
                NearestPlayerSensor(16.0, 0.0, 20),
                NearestEntitySensor(EntityGolem::class.java, CoreMemoryTypes.NEAREST_GOLEM, 42.0, 0.0)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 26
        super.initEntity()
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Witch"
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getDrops(): Array<Item> {
        val itemId = when (ThreadLocalRandom.current().nextInt(7)) {
            0 -> ItemID.STICK
            1 -> ItemID.SPIDER_EYE
            2 -> ItemID.GLOWSTONE_DUST
            3 -> ItemID.GUNPOWDER
            4 -> ItemID.REDSTONE
            5 -> ItemID.SUGAR
            else -> ItemID.GLASS_BOTTLE
        }
        return arrayOf(
            Item.get(itemId, 0, ThreadLocalRandom.current().nextInt(5))
        )
    }
}
