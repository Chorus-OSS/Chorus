package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockTurtleEgg
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.BlockSensor
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityAxolotl
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTrident
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function
import kotlin.math.min

class EntityDrowned(chunk: IChunk?, nbt: CompoundTag?) : EntityZombie(chunk, nbt), EntitySwimmable, EntityWalkable,
    EntitySmite {
    override fun getEntityIdentifier(): String {
        return EntityID.DROWNED
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    NearestBlockIncementExecutor(),
                    { entity: EntityMob? ->
                        !memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_BLOCK) && memoryStorage.get<Block>(
                            CoreMemoryTypes.NEAREST_BLOCK
                        ) is BlockTurtleEgg
                    }, 1, 1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    PlaySoundExecutor(Sound.MOB_DROWNED_SAY_WATER), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> isInsideOfWater() }), 11, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_DROWNED_SAY), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> !isInsideOfWater() }), 10, 1
                ),
                Behavior(
                    JumpExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob? -> !memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_BLOCK) },
                        IBehaviorEvaluator { entity: EntityMob ->
                            entity.getCollisionBlocks()!!.stream().anyMatch { block: Block? -> block is BlockTurtleEgg }
                        }), 9, 1, 10
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_BLOCK, 0.3f, true),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK),
                    8,
                    1
                ),
                Behavior(
                    TridentThrowExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 15, true, 30, 20), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        DistanceEvaluator(CoreMemoryTypes.ATTACK_TARGET, 32.0, 3.0),
                        IBehaviorEvaluator { entity: EntityMob? -> itemInHand.id == ItemID.TRIDENT }
                    ), 7, 1),
                Behavior(
                    TridentThrowExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 15, false, 30, 20), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_PLAYER, 32.0, 3.0),
                        IBehaviorEvaluator { itemInHand.id == ItemID.TRIDENT },
                        any(
                            IBehaviorEvaluator { level!!.isNight },
                            IBehaviorEvaluator {
                                memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]?.isInsideOfWater() ?: false
                            }
                        )
                    ), 6, 1),
                Behavior(
                    TridentThrowExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.3f,
                        15,
                        false,
                        30,
                        20
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET, 32.0, 3.0),
                        IBehaviorEvaluator { itemInHand.id == ItemID.TRIDENT }
                    ), 5, 1),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 40, true, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    4,
                    1
                ),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 40, false, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        any(
                            IBehaviorEvaluator {
                                level!!.isNight
                            },
                            IBehaviorEvaluator {
                                memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]?.isInsideOfWater() ?: false
                            }
                        )
                    ), 3, 1),
                Behavior(
                    MeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.3f,
                        40,
                        true,
                        30
                    ), EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET), 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, false, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                NearestPlayerSensor(64.0, 0.0, 0),
                NearestTargetEntitySensor<Entity>(
                    0.0, 16.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function<Entity, Boolean> { entity: Entity? ->
                        this.attackTarget(
                            entity!!
                        )
                    }),
                BlockSensor(BlockTurtleEgg::class.java, CoreMemoryTypes.NEAREST_BLOCK, 11, 15, 10)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun getFloatingForceFactor(): Double {
        if (any(
                EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK)
            ).evaluate(this)
        ) {
            if (hasWaterAt(this.getFloatingHeight())) {
                return 1.3
            }
            return 0.7
        }
        return 0.0
    }

    override fun initEntity() {
        this.maxHealth = 20
        this.diffHandDamage = floatArrayOf(2.5f, 3f, 4.5f)
        super.initEntity()
        memoryStorage.set<Boolean>(CoreMemoryTypes.ENABLE_DIVE_FORCE, true)
        val random = Utils.rand(0, 10000)
        if (random < 85) {
            setItemInHand(Item.get(ItemID.FISHING_ROD))
        } else if (random < 1585 && !wasTransformed()) {
            setItemInHand(Item.get(ItemID.TRIDENT))
        }
        if (Utils.rand(0, 100) < 3) {
            setItemInOffhand(Item.get(ItemID.NAUTILUS_SHELL))
        }
    }

    override fun getOriginalName(): String {
        return "Drowned"
    }

    override fun getDrops(): Array<Item> {
        var trident = Item.AIR
        if (itemInHand is ItemTrident) {
            val event = getLastDamageCause()
            var lootingLevel = 0
            if (event is EntityDamageByEntityEvent) {
                if (event.damager is EntityInventoryHolder) {
                    lootingLevel = event.damager.itemInHand.getEnchantmentLevel(Enchantment.ID_LOOTING)
                }
            }
            if (ThreadLocalRandom.current().nextInt(1, 100) < min(37.0, (25 + lootingLevel).toDouble())) {
                trident = Item.get(ItemID.TRIDENT)
            }
        }
        return arrayOf(
            Item.get(ItemID.ROTTEN_FLESH),
            trident
        )
    }

    override fun isUndead(): Boolean {
        return true
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun getExperienceDrops(): Int {
        return if (isBaby()) 7 else 5
    }

    fun wasTransformed(): Boolean {
        if (namedTag!!.contains("Transformed")) {
            return namedTag!!.getBoolean("Transformed")
        }
        return false
    }

    override fun attackTarget(entity: Entity): Boolean {
        return super.attackTarget(entity) || entity is EntityAxolotl
    }

    override fun transform() {
        throw UnsupportedOperationException("Drowned cannot transform")
    }
}
