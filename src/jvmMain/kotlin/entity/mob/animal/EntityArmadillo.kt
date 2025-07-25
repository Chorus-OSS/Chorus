package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.executor.armadillo.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestFeedingPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBrush
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils

class EntityArmadillo(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt) {

    private var rollState = RollState.UNROLLED

    override fun getEntityIdentifier(): String {
        return EntityID.ARMADILLO
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>( //用于刷新InLove状态的核心行为
                Behavior(
                    InLoveExecutor(400),
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_FEED_TIME, 0, 400),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_IN_LOVE_TIME, 6000, Int.MAX_VALUE)
                    ),
                    1, 1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    UnrollingExecutor(),
                    { entity: EntityMob? -> rollState == RollState.ROLLED_UP_UNROLLING }, 8, 1
                ),
                Behavior(
                    PeekExecutor(), any(
                        IBehaviorEvaluator { entity: EntityMob? -> rollState == RollState.ROLLED_UP_PEEKING },
                        all(
                            IBehaviorEvaluator { entity: EntityMob? -> rollState == RollState.ROLLED_UP_RELAXING },
                            ProbabilityEvaluator(1, 0xFFF)
                        )
                    ), 7, 1
                ),
                Behavior(RollUpExecutor(), RollupEvaluator(), 6, 1),
                Behavior(RelaxingExecutor(), ProbabilityEvaluator(1, 0xFFFF), 5, 1),
                Behavior(
                    ShedExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob? -> rollState == RollState.UNROLLED },
                        PassByTimeEvaluator(CoreMemoryTypes.NEXT_SHED, 0)
                    ), 5, 1
                ),
                Behavior(
                    EntityBreedingExecutor<EntityArmadillo>(EntityArmadillo::class.java, 16, 100, 0.5f),
                    { entity: EntityMob -> entity.memoryStorage.get<Boolean>(CoreMemoryTypes.IS_IN_LOVE) },
                    7,
                    1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_FEEDING_PLAYER, 0.4f, true), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_FEEDING_PLAYER),
                        IBehaviorEvaluator { entity: EntityMob? -> rollState == RollState.UNROLLED }
                    ), 2, 1),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100), all(
                        ProbabilityEvaluator(4, 10),
                        IBehaviorEvaluator { entity: EntityMob? -> rollState == RollState.UNROLLED }
                    ), 1, 1, 100),
                Behavior(
                    FlatRandomRoamExecutor(0.2f, 12, 20, false, -1, true, 40),
                    { entity: EntityMob? -> rollState == RollState.UNROLLED }, 1, 1
                )
            ),
            setOf<ISensor>(NearestFeedingPlayerSensor(8.0, 0.0), NearestPlayerSensor(8.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        val brush = player.inventory.getUnclonedItem(player.inventory.heldItemIndex)
        if (brush is ItemBrush) {
            level!!.dropItem(this.position, Item.get(ItemID.ARMADILLO_SCUTE))
            level!!.addSound(player.position, Sound.MOB_ARMADILLO_BRUSH)
            brush.incDamage(16)
            if (brush.damage >= brush.maxDurability) {
                player.level!!.addSound(player.position, Sound.RANDOM_BREAK)
                player.inventory.clear(player.inventory.heldItemIndex)
            }
        }
        return super.onInteract(player, item, clickedPos)
    }

    override fun initEntity() {
        this.maxHealth = 12
        super.initEntity()
        setMovementSpeedF(0.5f)
        setRollState(RollState.UNROLLED)
        memoryStorage.set<Int>(
            CoreMemoryTypes.NEXT_SHED,
            level!!.tick + Utils.rand(6000, 10800)
        )
    }

    override fun getWidth(): Float {
        if (isBaby()) {
            return 0.42f
        }
        return 0.7f
    }

    override fun getHeight(): Float {
        if (isBaby()) {
            return 0.39f
        }
        return 0.65f
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (rollState != RollState.UNROLLED) {
            source.damage = (source.damage - 1) / 2f
        }
        return super.attack(source)
    }

    override fun isBreedingItem(item: Item): Boolean {
        return item.id == ItemID.SPIDER_EYE
    }

    fun setRollState(state: RollState) {
        this.rollState = state
        setEnumEntityProperty(PROPERTY_STATE, state.state)
        sendData(viewers.values.toTypedArray())
        setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED, rollState != RollState.UNROLLED)
    }


    enum class RollState(val state: String) {
        UNROLLED("unrolled"),
        ROLLED_UP("rolled_up"),
        ROLLED_UP_PEEKING("rolled_up_peeking"),
        ROLLED_UP_RELAXING("rolled_up_relaxing"),
        ROLLED_UP_UNROLLING("rolled_up_unrolling")
    }

    class RollupEvaluator : IBehaviorEvaluator {
        override fun evaluate(entity: EntityMob): Boolean {
            return AnyMatchEvaluator(
                PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, 0, 1),
                IBehaviorEvaluator { entity1: EntityMob? ->
                    for (other in entity.level!!.getCollidingEntities(
                        entity.getBoundingBox().grow(7.0, 2.0, 7.0)
                    )) {
                        if (other is EntityMonster) {
                            if (other.isUndead()) return@IBehaviorEvaluator true
                        } else if (other is Player) {
                            if (other.isSprinting()) return@IBehaviorEvaluator true
                        }
                    }
                    false
                }
            ).evaluate(entity)
        }
    }

    companion object {
        private const val PROPERTY_STATE = "minecraft:armadillo_state"
    }
}
