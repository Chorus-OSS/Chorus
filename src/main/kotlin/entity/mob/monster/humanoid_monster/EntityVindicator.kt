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
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityHoglin
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

class EntityVindicator(chunk: IChunk?, nbt: CompoundTag?) : EntityIllager(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.VINDICATOR
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    PlaySoundExecutor(
                        Sound.MOB_VINDICATOR_IDLE,
                        if (isBaby()) 1.3f else 0.8f,
                        if (isBaby()) 1.7f else 1.2f,
                        1f,
                        1f
                    ), RandomSoundEvaluator(), 7, 1
                ),
                Behavior(
                    VindicatorMeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.5f, 40, true, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    4,
                    1
                ),
                Behavior(
                    VindicatorMeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.5f, 40, false, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    3,
                    1
                ),
                Behavior(
                    VindicatorMeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.5f,
                        40,
                        true,
                        30
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET), 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.5f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                NearestTargetEntitySensor<Entity>(
                    0.0, 16.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function<Entity, Boolean> { entity: Entity? ->
                        this.attackTarget(
                            entity!!
                        )
                    }),
                NearestPlayerSensor(16.0, 0.0, 0)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }


    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier()) {
            EntityID.IRON_GOLEM, EntityID.SNOW_GOLEM, EntityID.VILLAGER, EntityID.WANDERING_TRADER -> true
            else -> super.attackTarget(entity) || isJohnny()
        }
    }

    override fun initEntity() {
        this.maxHealth = 24
        this.diffHandDamage = floatArrayOf(3.5f, 5f, 7.5f)
        super.initEntity()
        setItemInHand(Item.get(ItemID.IRON_AXE))
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Vindicator"
    }

    override fun getExperienceDrops(): Int {
        return Math.toIntExact(
            if (isBaby()) 1 else 5 + (equipment.contents.values.stream().filter { obj: Item -> obj.isArmor }
                .count() * ThreadLocalRandom.current().nextInt(1, 4)))
    }

    override fun getDrops(): Array<Item> {
        val axe = Item.get(ItemID.IRON_AXE)
        axe.damage = ThreadLocalRandom.current().nextInt(1, axe.maxDurability)
        return arrayOf(
            axe,
            Item.get(ItemID.EMERALD, 0, ThreadLocalRandom.current().nextInt(2))
        )
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    protected class VindicatorMeleeAttackExecutor(
        memory: NullableMemoryType<out Entity>,
        speed: Float,
        maxSenseRange: Int,
        clearDataWhenLose: Boolean,
        coolDown: Int
    ) :
        MeleeAttackExecutor(memory, speed, maxSenseRange, clearDataWhenLose, coolDown) {
        override fun onStart(entity: EntityMob) {
            super.onStart(entity)
            entity.setDataProperty(EntityDataTypes.TARGET_EID, entity.memoryStorage[memory]!!.getRuntimeID())
            entity.setDataFlag(EntityFlag.ANGRY)
            entity.level!!.addLevelSoundEvent(
                entity.position,
                LevelSoundEventPacket.SOUND_ANGRY,
                -1,
                EntityID.VINDICATOR,
                false,
                false
            )
            entity.level!!.entities.values.filter { entity1: Entity ->
                entity1 is EntityPiglin && entity1.position.distance(entity.position) < 16 && entity1.memoryStorage.isEmpty(
                    CoreMemoryTypes.ATTACK_TARGET
                )
            }.forEach { entity1: Entity ->
                (entity1 as EntityPiglin).memoryStorage[CoreMemoryTypes.ATTACK_TARGET] =
                    entity.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
            }
            if (entity.memoryStorage.get<Entity>(CoreMemoryTypes.ATTACK_TARGET) is EntityHoglin) {
                entity.memoryStorage.set<Int>(CoreMemoryTypes.LAST_HOGLIN_ATTACK_TIME, entity.level!!.tick)
            }
        }

        override fun onStop(entity: EntityMob) {
            super.onStop(entity)
            entity.setDataFlag(EntityFlag.ANGRY, false)
            entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
        }

        override fun onInterrupt(entity: EntityMob) {
            super.onInterrupt(entity)
            entity.setDataFlag(EntityFlag.ANGRY, false)
            entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
        }
    }

    fun isJohnny(): Boolean {
        return getNameTag() == "Johnny" || (namedTag!!.exist("Johnny") && namedTag!!.getBoolean("Johnny"))
    }
}
