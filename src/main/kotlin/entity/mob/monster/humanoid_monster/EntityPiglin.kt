package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockDoor
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockSoulFire
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
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.*
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.mob.EntityHoglin
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.EntityZoglin
import org.chorus_oss.chorus.entity.mob.monster.EntityWither
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.item.*
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import java.util.function.Function

open class EntityPiglin(chunk: IChunk?, nbt: CompoundTag?) : EntityHumanoidMonster(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.PIGLIN
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    PiglinTransformExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob -> entity.level!!.dimension != Level.DIMENSION_NETHER },
                        IBehaviorEvaluator { entity: EntityMob? -> !isImmobile() },
                        IBehaviorEvaluator { entity: EntityMob -> !entity.namedTag!!.getBoolean("IsImmuneToZombification") }
                    ), 13, 1),
                Behavior(
                    PiglinTradeExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob? -> !itemInOffhand.isNothing },
                        IBehaviorEvaluator { entity: EntityMob? -> !isAngry() },
                        not(
                            all(
                                IBehaviorEvaluator { entity: EntityMob ->
                                    entity.level!!.tick - memoryStorage[CoreMemoryTypes.LAST_BE_ATTACKED_TIME] <= 1
                                }
                            )
                        )
                    ), 12, 1),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_PIGLIN_JEALOUS, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob ->
                            viewers.values.stream().noneMatch { p: Player ->
                                p.position.distance(entity.position) < 8 && likesItem(p.inventory.itemInHand) && p.level!!.raycastBlocks(
                                    p.position,
                                    entity.position
                                ).isEmpty()
                            }
                        }), 12, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_PIGLIN_ANGRY, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> isAngry() }), 11, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_PIGLIN_AMBIENT, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> !isAngry() }), 10, 1
                ),
                Behavior(
                    CrossBowShootExecutor(
                        { this.itemInHand },
                        CoreMemoryTypes.ATTACK_TARGET,
                        0.3f,
                        15,
                        true,
                        30,
                        80
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob? -> itemInHand is ItemCrossbow }
                    ), 9, 1),
                Behavior(
                    CrossBowShootExecutor(
                        { this.itemInHand },
                        CoreMemoryTypes.NEAREST_PLAYER,
                        0.3f,
                        15,
                        true,
                        30,
                        80
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        IBehaviorEvaluator {
                            val player = memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]
                            player is Player && !
                            player.inventory.armorContents.toList().stream()
                                .anyMatch { item -> !item.isNothing && item is ItemArmor && item.tier == ItemArmor.TIER_GOLD }
                        },
                        IBehaviorEvaluator { itemInHand is ItemCrossbow }
                    ), 8, 1),
                Behavior(
                    CrossBowShootExecutor(
                        { this.itemInHand },
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.3f,
                        15,
                        true,
                        30,
                        80
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob -> !entity.namedTag!!.getBoolean("CannotHunt") },
                        IBehaviorEvaluator { entity: EntityMob? -> itemInHand is ItemCrossbow },
                        any(
                            IBehaviorEvaluator { entity: EntityMob? -> memoryStorage.get<Int>(CoreMemoryTypes.LAST_HOGLIN_ATTACK_TIME) == 0 },
                            PassByTimeEvaluator(CoreMemoryTypes.LAST_HOGLIN_ATTACK_TIME, 6000)
                        )
                    ), 7, 1
                ),
                Behavior(
                    PiglinMeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.5f, 40, true, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET)
                    ), 6, 1
                ),
                Behavior(
                    PiglinMeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.5f, 40, false, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        IBehaviorEvaluator {
                            val player = memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]
                            player is Player && !
                            player.inventory.armorContents.toList().stream()
                                .anyMatch { item: Item -> !item.isNothing && item is ItemArmor && item.tier == ItemArmor.TIER_GOLD }
                        }
                    ), 5, 1),
                Behavior(
                    PiglinMeleeAttackExecutor(
                        CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET,
                        0.5f,
                        40,
                        true,
                        30
                    ), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob -> !entity.namedTag!!.getBoolean("CannotHunt") },
                        any(
                            IBehaviorEvaluator { entity: EntityMob? -> memoryStorage.get<Int>(CoreMemoryTypes.LAST_HOGLIN_ATTACK_TIME) == 0 },
                            PassByTimeEvaluator(CoreMemoryTypes.LAST_HOGLIN_ATTACK_TIME, 6000)
                        )
                    ), 5, 1
                ),
                Behavior(
                    PiglinFleeFromTargetExecutor(CoreMemoryTypes.NEAREST_BLOCK), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK),
                        IBehaviorEvaluator { entity: EntityMob? -> memoryStorage.get<Block>(CoreMemoryTypes.NEAREST_BLOCK) is BlockSoulFire }
                    ), 2, 1),
                Behavior(
                    PiglinFleeFromTargetExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                        IBehaviorEvaluator { entity: EntityMob? ->
                            if (isBaby()) {
                                return@IBehaviorEvaluator true
                            } else {
                                val entity1 =
                                    memoryStorage.get<Entity>(CoreMemoryTypes.NEAREST_SHARED_ENTITY)
                                return@IBehaviorEvaluator !(entity1 is EntityWither || entity1 is EntityWitherSkeleton)
                            }
                        }
                    ), 3, 1),
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
                    }),
                NearestPlayerAngryPiglinSensor(),
                NearestEntitySensor(
                    EntityZombiePigman::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    8.0,
                    0.0
                ),
                NearestEntitySensor(
                    EntityZoglin::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    8.0,
                    0.0
                ),
                NearestEntitySensor(
                    EntityWither::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    8.0,
                    0.0
                ),
                NearestEntitySensor(
                    EntityWitherSkeleton::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    8.0,
                    0.0
                ),
                BlockSensor(BlockDoor::class.java, CoreMemoryTypes.NEAREST_BLOCK, 2, 2, 20),
                BlockSensor(BlockSoulFire::class.java, CoreMemoryTypes.NEAREST_BLOCK, 8, 2, 20)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }


    override fun getFloatingForceFactor(): Double {
        return 0.0
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (itemInOffhand.isNothing && !isAngry()) {
            if (item is ItemGoldIngot) {
                if (player.gamemode != Player.CREATIVE) player.inventory.decreaseCount(player.inventory.heldItemIndex)
                setItemInOffhand(Item.get(ItemID.GOLD_INGOT))
            }
        }
        return super.onInteract(player, item)
    }

    override fun initEntity() {
        this.maxHealth = 16
        this.diffHandDamage = floatArrayOf(3f, 5f, 7f)
        super.initEntity()
        if (!isBaby()) setItemInHand(Item.get(if (Utils.rand()) ItemID.GOLDEN_SWORD else ItemID.CROSSBOW))
        if (Utils.rand(0, 10) == 0) setHelmet(Item.get(ItemID.GOLDEN_HELMET))
        if (Utils.rand(0, 10) == 0) setChestplate(Item.get(ItemID.GOLDEN_CHESTPLATE))
        if (Utils.rand(0, 10) == 0) setLeggings(Item.get(ItemID.GOLDEN_LEGGINGS))
        if (Utils.rand(0, 10) == 0) setBoots(Item.get(ItemID.GOLDEN_BOOTS))
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (currentTick % 20 == 0) {
            if (level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)) {
                pickupItems(this)
            }
        }
        return super.onUpdate(currentTick)
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Piglin"
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return !this.isBaby()
    }

    fun isAngry(): Boolean {
        return getDataFlag(EntityFlag.ANGRY)
    }

    override fun getDrops(): Array<Item> {
        val drops: MutableList<Item> = ArrayList()
        if (ThreadLocalRandom.current().nextInt(200) < 17) { // 8.5%
            drops.add(itemInHand)
            drops.addAll(equipment.getArmor())
        }
        drops.add(itemInOffhand)
        return drops.toTypedArray()
    }

    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier()) {
            EntityID.WITHER_SKELETON, EntityID.WITHER -> true
            EntityID.HOGLIN -> {
                if (entity is EntityHoglin) {
                    if (!entity.isBaby()) {
                        if (entity.health - getDiffHandDamage(Server.instance.getDifficulty()) <= 0) {
                            val entities = level!!.entities.values.filter { entity1: Entity ->
                                entity1 is EntityPiglin && entity1.position.distance(
                                    this.position
                                ) < 16
                            }.toList()
                            val packet = org.chorus_oss.protocol.packets.AnimateEntityPacket(
                                animation = "animation.piglin.celebrate_hunt_special",
                                nextState = "r",
                                stopExpression = "query.any_animation_finished",
                                stopExpressionVersion = 16777216,
                                controller = "__runtime_controller",
                                blendOutTime = 1f,
                                runtimeIDs = entities.map { it.getRuntimeID().toULong() }
                            )
                            playAnimationOnEntities(packet, entities)
                            entities.forEach(Consumer { entity1: Entity ->
                                entity1.level!!.addSound(
                                    entity1.position,
                                    Sound.MOB_PIGLIN_CELEBRATE
                                )
                            })
                        }
                    }
                }
                false
            }

            else -> false
        }
    }

    fun pickupItems(entity: Entity) {
        if (!isAngry()) {
            if (entity is EntityInventoryHolder) {
                for (i in entity.level!!.getNearbyEntities(
                    entity.getBoundingBox().grow(1.0, 0.5, 1.0)
                )) {
                    var pickup = false
                    if (i is EntityItem) {
                        val item = i.item
                        if ((item.isArmor || item.isTool) && item.tier == ItemTool.TIER_GOLD) {
                            if (entity.equip(item)) {
                                pickup = true
                            }
                        } else if (item is ItemPorkchop) {
                            pickup = true
                        } else if (likesItem(item)) {
                            if (itemInOffhand.isNothing) {
                                setItemInOffhand(item)
                                pickup = true
                            }
                        }
                        if (pickup) {
                            val pk = org.chorus_oss.protocol.packets.TakeItemEntityPacket(
                                itemEntityRuntimeID = i.getRuntimeID().toULong(),
                                takerEntityRuntimeID = entity.getRuntimeID().toULong(),
                            )
                            Server.broadcastPacket(entity.viewers.values, pk)
                            i.close()
                        }
                    }
                }
            }
        }
    }

    override fun getExperienceDrops(): Int {
        return Math.toIntExact(if (isBaby()) 1 else 5 + (equipment.getArmor().stream().filter { it.isArmor }
            .count() * ThreadLocalRandom.current().nextInt(1, 4)))
    }

    override fun equip(item: Item): Boolean {
        if ((item.tier > itemInHand.tier && itemInHand.tier != ItemArmor.TIER_GOLD) || item.tier == ItemArmor.TIER_GOLD) {
            level!!.dropItem(this.position, itemInHand)
            this.setItemInHand(item)
            return true
        }
        return false
    }

    protected class PiglinFleeFromTargetExecutor(memory: NullableMemoryType<out IVector3>) :
        FleeFromTargetExecutor(memory, 0.5f, true, 8f) {
        override fun onStart(entity: EntityMob) {
            super.onStart(entity)
            if (entity.position.distance(entity.memoryStorage[memory]!!.vector3) < 8) {
                entity.level!!.addSound(entity.position, Sound.MOB_PIGLIN_RETREAT)
            }
        }
    }

    protected class PiglinMeleeAttackExecutor(
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
                EntityID.PIGLIN,
                false,
                false
            )
            (entity.level!!.entities.values).filter { entity1: Entity ->
                entity1 is EntityPiglin && entity1.position.distance(entity.position) < 16 && entity1.memoryStorage
                    .isEmpty(CoreMemoryTypes.ATTACK_TARGET)
            }.forEach { entity1: Entity ->
                (entity1 as EntityPiglin).memoryStorage[CoreMemoryTypes.ATTACK_TARGET] =
                    entity.memoryStorage[memory]
            }
            if (entity.memoryStorage.get(memory) is EntityHoglin) {
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

    companion object {
        fun likesItem(item: Item): Boolean {
            return when (item.id) {
                BlockID.BELL,
                BlockID.GOLD_BLOCK,
                ItemID.CLOCK,
                ItemID.ENCHANTED_GOLDEN_APPLE,
                BlockID.GILDED_BLACKSTONE,
                ItemID.GLISTERING_MELON_SLICE,
                ItemID.GOLD_INGOT,
                ItemID.GOLD_NUGGET,
                BlockID.GOLD_ORE,
                ItemID.GOLDEN_APPLE,
                ItemID.GOLDEN_AXE,
                ItemID.GOLDEN_BOOTS,
                ItemID.GOLDEN_CARROT,
                ItemID.GOLDEN_CHESTPLATE,
                ItemID.GOLDEN_HELMET,
                ItemID.GOLDEN_HOE,
                ItemID.GOLDEN_HORSE_ARMOR,
                ItemID.GOLDEN_LEGGINGS,
                ItemID.GOLDEN_PICKAXE,
                ItemID.GOLDEN_SHOVEL,
                ItemID.GOLDEN_SWORD,
                BlockID.LIGHT_WEIGHTED_PRESSURE_PLATE,
                BlockID.NETHER_GOLD_ORE,
                BlockID.GOLDEN_RAIL,
                ItemID.RAW_GOLD -> true

                else -> false
            }
        }
    }
}
