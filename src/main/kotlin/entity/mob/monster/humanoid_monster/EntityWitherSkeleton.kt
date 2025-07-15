package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
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
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.Utils
import java.util.function.Function

class EntityWitherSkeleton(chunk: IChunk?, nbt: CompoundTag?) : EntitySkeleton(chunk, nbt), EntityWalkable,
    EntitySmite {
    override fun getEntityIdentifier(): String {
        return EntityID.WITHER_SKELETON
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    IBehaviorExecutor { entity: EntityMob? ->
                        val storage = memoryStorage
                        if (storage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) return@IBehaviorExecutor false
                        var attackTarget: Entity? = null
                        if (storage.notEmpty(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET) && storage.get<Entity>(
                                CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET
                            )!!.isAlive()
                        ) {
                            attackTarget = storage.get<Entity>(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET)
                        }
                        storage[CoreMemoryTypes.ATTACK_TARGET] = attackTarget
                        false
                    },
                    { entity: EntityMob? -> true }, 20
                )
            ),
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_WITHER_AMBIENT), RandomSoundEvaluator(), 5, 1),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.ATTACK_TARGET, 0.3f, 40, true, 10, Effect.get(
                            EffectType.WITHER
                        ).setDuration(200)
                    ), EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET), 4, 1
                ),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 40, false, 10, Effect.get(
                            EffectType.WITHER
                        ).setDuration(200)
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER), 2, 1
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
                    })
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }


    //凋零骷髅会攻击距离他16格范围内的玩家、雪傀儡、小海龟、铁傀儡、猪灵或猪灵蛮兵
    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier()) {
            EntityID.SNOW_GOLEM, EntityID.IRON_GOLEM, EntityID.TURTLE, EntityID.PIGLIN, EntityID.PIGLIN_BRUTE -> true
            else -> false
        }
    }

    override fun initEntity() {
        this.maxHealth = 20
        this.diffHandDamage = floatArrayOf(5f, 8f, 12f)
        super.initEntity()
        // 判断凋零骷髅是否手持石剑如果没有就给它石剑
        if (this.itemInHand !== Item.get(ItemID.STONE_SWORD)) {
            this.setItemInHand(Item.get(ItemID.STONE_SWORD))
        }
        // 设置凋零骷髅空闲状态播放空闲声音
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_EVENT_NAME, LevelSoundEventPacket.SOUND_AMBIENT)
    }

    override fun getWidth(): Float {
        return 0.7f
    }

    override fun getHeight(): Float {
        return 2.4f
    }

    override fun getOriginalName(): String {
        return "Wither Skeleton"
    }

    //掉落剑的概率为8.5% 掉落头的概率为2.5%
    override fun getDrops(): Array<Item> {
        val drops: MutableList<Item> = ArrayList()
        drops.add(Item.get(ItemID.BONE, 0, Utils.rand(0, 2)))
        if (Utils.rand(0, 2) == 0) {
            drops.add(Item.get(ItemID.COAL, 0, 1))
        }
        //掉落石剑的概率为8.5%
        if (Utils.rand(0, 200) <= 17) {
            drops.add(Item.get(ItemID.STONE_SWORD, Utils.rand(0, 131), 1))
        }
        //掉落头的概率为2.5%
        if (Utils.rand(0, 40) == 1) {
            drops.add(Item.get(BlockID.SKULL, 1, 1))
        }
        return drops.toTypedArray()
    }
}
