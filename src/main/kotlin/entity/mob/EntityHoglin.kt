package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockPortal
import org.chorus_oss.chorus.block.BlockRespawnAnchor
import org.chorus_oss.chorus.block.BlockWarpedFungus
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
import org.chorus_oss.chorus.entity.ai.sensor.BlockSensor
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.Utils

class EntityHoglin(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.HOGLIN
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    InLoveExecutor(400),
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_FEED_TIME, 0, 400),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_IN_LOVE_TIME, 6000, Int.MAX_VALUE)
                    ), 1, 1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    EntityBreedingExecutor<EntityHoglin>(EntityHoglin::class.java, 16, 100, 0.5f),
                    IBehaviorEvaluator { entity: EntityMob ->
                        entity.memoryStorage.get<Boolean>(CoreMemoryTypes.IS_IN_LOVE)
                    }, 9, 1
                ),
                Behavior(
                    HoglinTransformExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob -> entity.level!!.dimension != Level.DIMENSION_NETHER },
                        IBehaviorEvaluator { entity: EntityMob? -> !isImmobile() },
                        IBehaviorEvaluator { entity: EntityMob -> !entity.namedTag!!.getBoolean("IsImmuneToZombification") }
                    ), 8, 1),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_HOGLIN_ANGRY, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> isAngry() }), 7, 1
                ),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_HOGLIN_AMBIENT, 0.8f, 1.2f, 0.8f, 0.8f), all(
                        RandomSoundEvaluator(),
                        IBehaviorEvaluator { entity: EntityMob? -> !isAngry() }), 6, 1
                ),
                Behavior(
                    HoglinMeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.5f, 40, true, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        not(IBehaviorEvaluator { entity: EntityMob? ->
                            val player = memoryStorage.get<Entity>(CoreMemoryTypes.ATTACK_TARGET)
                            player is Player && isBreedingItem(
                                player.inventory.itemInHand
                            )
                        })
                    ), 5, 1
                ),
                Behavior(
                    HoglinMeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.5f, 40, false, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        not(IBehaviorEvaluator { entity: EntityMob? ->
                            val player = memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]
                            player is Player && isBreedingItem(
                                player.inventory.itemInHand
                            )
                        })
                    ), 4, 1
                ),
                Behavior(
                    HoglinFleeFromTargetExecutor(CoreMemoryTypes.NEAREST_BLOCK), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK)
                    ), 3, 1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.PARENT, 0.7f, true, 64f, 3f), all(
                        EntityCheckEvaluator(CoreMemoryTypes.PARENT),
                        DistanceEvaluator(CoreMemoryTypes.PARENT, 5.0),
                        IBehaviorEvaluator { entity: EntityMob? -> isBaby() }
                    ), 2, 1),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                NearestPlayerSensor(40.0, 0.0, 20),
                BlockSensor(BlockPortal::class.java, CoreMemoryTypes.NEAREST_BLOCK, 8, 2, 20),
                BlockSensor(BlockWarpedFungus::class.java, CoreMemoryTypes.NEAREST_BLOCK, 8, 2, 20),
                BlockSensor(BlockRespawnAnchor::class.java, CoreMemoryTypes.NEAREST_BLOCK, 8, 2, 20)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    fun isAngry(): Boolean {
        return getDataFlag(EntityFlag.ANGRY)
    }

    override fun initEntity() {
        this.setMaxHealth(40)
        this.diffHandDamage = floatArrayOf(1f, 1f, 1f)
        super.initEntity()
    }

    override var diffHandDamage: FloatArray
        get() {
            return if (isBaby()) {
                super.diffHandDamage
            } else floatArrayOf(
                Utils.rand(2.5f, 5f),
                Utils.rand(3f, 8f),
                Utils.rand(4.5f, 12f),
            )
        }
        set(value) {
            super.diffHandDamage = value
        }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.85f
        }
        return 1.4f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.85f
        }
        return 1.4f
    }

    fun isBreedingItem(item: Item): Boolean {
        return item.id == BlockID.CRIMSON_FUNGUS
    }

    override fun getOriginalName(): String {
        return "Hoglin"
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(
            Item.get(
                (if (this.isOnFire()) ItemID.COOKED_PORKCHOP else ItemID.PORKCHOP),
                0,
                Utils.rand(1, 3)
            )
        )
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        val superResult: Boolean = super.onInteract(player, item, clickedPos)
        if (isBreedingItem(item)) {
            memoryStorage[CoreMemoryTypes.LAST_FEED_PLAYER] = player
            memoryStorage[CoreMemoryTypes.LAST_BE_FEED_TIME] = level!!.tick
            sendBreedingAnimation(item)
            item.count--
            return player.inventory.setItemInHand(item) && superResult
        }
        return superResult
    }

    protected fun sendBreedingAnimation(item: Item) {
        val pk: EntityEventPacket = EntityEventPacket()
        pk.event = EntityEventPacket.EATING_ITEM
        pk.eid = this.getRuntimeID()
        pk.data = item.fullId
        Server.broadcastPacket(viewers.values, pk)
    }

    override fun getExperienceDrops(): Int {
        return if (isBaby()) 0 else Utils.rand(1, 3)
    }

    protected class HoglinFleeFromTargetExecutor(memory: NullableMemoryType<out IVector3>) :
        FleeFromTargetExecutor(memory, 0.5f, true, 8f) {
        override fun onStart(entity: EntityMob) {
            super.onStart(entity)
            if (entity.position.distance(entity.memoryStorage[memory]!!.vector3) < 8) {
                entity.level!!.addSound(entity.position, Sound.MOB_HOGLIN_RETREAT)
            }
        }
    }

    protected class HoglinMeleeAttackExecutor(
        memory: NullableMemoryType<out Entity>,
        speed: Float,
        maxSenseRange: Int,
        clearDataWhenLose: Boolean,
        coolDown: Int
    ) :
        MeleeAttackExecutor(memory, speed, maxSenseRange, clearDataWhenLose, coolDown) {
        override fun onStart(entity: EntityMob) {
            super.onStart(entity)
            entity.setDataProperty(
                EntityDataTypes.TARGET_EID,
                entity.memoryStorage[memory]!!.getRuntimeID()
            )
            entity.setDataFlag(EntityFlag.ANGRY)
            entity.level!!.addLevelSoundEvent(
                entity.position,
                LevelSoundEventPacket.SOUND_ANGRY,
                -1,
                EntityID.HOGLIN,
                false,
                false
            )
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
}
