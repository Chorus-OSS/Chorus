package org.chorus_oss.chorus.entity.mob.monster


import org.chorus_oss.chorus.Player
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
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.PassByTimeEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.FlyingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillager
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

class EntityVex(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityFlyable {
    override fun getEntityIdentifier(): String {
        return EntityID.VEX
    }


    var illager: EntityEvocationIllager? = null
    private val start_damage_timer = ThreadLocalRandom.current().nextInt(30, 120)


    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_VEX_AMBIENT), RandomSoundEvaluator(), 5, 1),
                Behavior(
                    VexMeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 40, true, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 80, Int.MAX_VALUE)
                    ), 4, 1
                ),
                Behavior(
                    VexMeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.3f,
                        40,
                        true,
                        30
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 80, Int.MAX_VALUE)
                    ), 3, 1
                ),
                Behavior(
                    VexMeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 40, false, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_ATTACK_TIME, 80, Int.MAX_VALUE)
                    ), 2, 1
                ),
                Behavior(
                    SpaceRandomRoamExecutor(0.15f, 12, 100, 20, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(
                NearestPlayerSensor(70.0, 0.0, 20),
                NearestTargetEntitySensor<Entity>(
                    0.0, 70.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function<Entity, Boolean> { entity: Entity? ->
                        this.attackTarget(
                            entity!!
                        )
                    })
            ),
            setOf<IController>(SpaceMoveController(), LookController(true, true), LiftController()),
            SimpleSpaceAStarRouteFinder(FlyingPosEvaluator(), this),
            this
        )
    }

    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier()) {
            EntityID.VILLAGER -> entity is EntityVillager && !entity.isBaby()
            EntityID.IRON_GOLEM, EntityID.WANDERING_TRADER -> true
            else -> false
        }
    }

    override fun initEntity() {
        this.maxHealth = 14
        super.initEntity()
        memoryStorage.set<Int>(CoreMemoryTypes.LAST_ATTACK_TIME, level!!.tick)
        this.setItemInHand(Item.get(ItemID.IRON_SWORD))
        this.diffHandDamage = floatArrayOf(5.5f, 9f, 13.5f)
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (source.cause == DamageCause.SUFFOCATION) {
            return false
        }
        return super.attack(source)
    }

    override fun getWidth(): Float {
        return 0.4f
    }

    override fun getHeight(): Float {
        return 0.8f
    }

    override fun getOriginalName(): String {
        return "Vex"
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getDrops(): Array<Item> {
        if (itemInHand is ItemTool) {
            val tool = itemInHand as ItemTool
            tool.damage = (ThreadLocalRandom.current().nextInt(tool.maxDurability))
            return arrayOf(
                tool
            )
        }
        return super.getDrops()
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed) return true
        if (this.illager != null) {
            if (position.distanceSquared(illager!!.position) > 56.25) {
                moveTarget = this.illager!!.position
                lookTarget = this.illager!!.position
            }
            if (ticksLived % 20 == 0) {
                if (ticksLived >= start_damage_timer * 20) {
                    this.attack(EntityDamageEvent(this, DamageCause.AGE, 1f))
                }
            }
        }
        return super.onUpdate(currentTick)
    }

    private inner class VexMeleeAttackExecutor(
        memory: NullableMemoryType<out Entity>,
        speed: Float,
        maxSenseRange: Int,
        clearDataWhenLose: Boolean,
        coolDown: Int
    ) :
        MeleeAttackExecutor(memory, speed, maxSenseRange, clearDataWhenLose, coolDown, 1f) {
        override fun onStart(entity: EntityMob) {
            super.onStart(entity)
            entity.setDataFlag(EntityFlag.CHARGING)
            entity.level!!.addSound(entity.position, Sound.MOB_VEX_CHARGE)
        }

        override fun onStop(entity: EntityMob) {
            super.onStop(entity)
            entity.setDataFlag(EntityFlag.CHARGING, false)
        }

        override fun onInterrupt(entity: EntityMob) {
            super.onInterrupt(entity)
            entity.setDataFlag(EntityFlag.CHARGING, false)
        }
    }
}
