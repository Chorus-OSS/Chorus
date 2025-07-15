package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockBeehive
import org.chorus_oss.chorus.block.BlockFlower
import org.chorus_oss.chorus.block.BlockWitherRose
import org.chorus_oss.chorus.blockentity.BlockEntityBeehive
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
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.executor.BeeAttackExecutor
import org.chorus_oss.chorus.entity.ai.executor.MoveToTargetExecutor
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.FlyingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.MemorizedBlockSensor
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*

class EntityBee(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityFlyable {
    override fun getEntityIdentifier(): String {
        return EntityID.BEE
    }

    private var hasNectar = false

    private var stayAtFlower = false

    var dieInTicks: Int = -1

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    BeeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.7f, 33, true, 20),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob? -> hasSting() }
                    ), 6, 1),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_BLOCK, 0.22f, true),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_BLOCK),
                    4,
                    1
                ),
                Behavior(
                    SpaceRandomRoamExecutor(0.15f, 12, 100, 20, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(
                MemorizedBlockSensor(20, 5, 20)
            ),
            setOf<IController>(SpaceMoveController(), LookController(true, true), LiftController()),
            SimpleSpaceAStarRouteFinder(FlyingPosEvaluator(), this),
            this
        )
    }

    fun hasSting(): Boolean {
        return dieInTicks == -1
    }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.275f
        }
        return 0.55f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.25f
        }
        return 0.5f
    }

    fun hasNectar(): Boolean {
        return this.hasNectar
    }

    fun setNectar(hasNectar: Boolean) {
        this.hasNectar = hasNectar
    }

    fun isAngry(): Boolean {
        return memoryStorage.get<Boolean>(CoreMemoryTypes.IS_ANGRY)
    }

    fun setAngry(angry: Boolean) {
        memoryStorage[CoreMemoryTypes.IS_ANGRY] = angry
        setDataFlag(EntityFlag.ANGRY, angry)
    }

    fun setAngry(entity: Entity?) {
        setAngry(true)
        memoryStorage[CoreMemoryTypes.ATTACK_TARGET] = entity
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (source.cause == DamageCause.SUFFOCATION) {
            if (ticksLived < 10) {
                source.cancelled = true
                return false
            }
        }
        if (source is EntityDamageByEntityEvent) {
            for (entity in level!!.getCollidingEntities(
                getBoundingBox()
                    .grow(4.0, 4.0, 4.0)
            )) {
                if (entity is EntityBee && entity.hasSting()) {
                    entity.setAngry(source.damager)
                }
            }
        }
        return super.attack(source)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (!hasSting() && isAlive()) {
            dieInTicks--
            if (dieInTicks < 0) {
                kill()
            }
        }
        if (currentTick % 20 == 0 && hasSting()) {
            memoryStorage[CoreMemoryTypes.LOOKING_BLOCK] =
                if (shouldSearchBeehive()) BlockBeehive::class.java else BlockFlower::class.java
            val blockClass = this.memoryStorage[CoreMemoryTypes.LOOKING_BLOCK]!!
            if (blockClass.isAssignableFrom(BlockFlower::class.java)) {
                Arrays.stream(level!!.getCollisionBlocks(getBoundingBox().grow(1.5, 1.5, 1.5), false, true))
                    .filter { block: Block? -> block is BlockFlower }
                    .findAny().ifPresent { flower: Block? ->
                        if (flower is BlockWitherRose) {
                            this.kill()
                        } else if (stayAtFlower) {
                            this.setNectar(true)
                            level!!.addSound(this.position, Sound.MOB_BEE_POLLINATE)
                        }
                        stayAtFlower = !stayAtFlower
                    }
            } else if (blockClass.isAssignableFrom(BlockBeehive::class.java)) {
                Arrays.stream(level!!.getCollisionBlocks(getBoundingBox().grow(1.5, 1.5, 1.5), false, true))
                    .filter { block: Block ->
                        if (block is BlockBeehive) {
                            val hiveEntity = block.getOrCreateBlockEntity()
                            return@filter !hiveEntity.isHoneyFull && hiveEntity.occupantsCount < 3
                        }
                        false
                    }
                    .findAny().ifPresent { block: Block ->
                        val hiveEntity = (block as BlockBeehive).getOrCreateBlockEntity()
                        hiveEntity.addOccupant(this)
                    }
            }
        }
        return super.onUpdate(currentTick)
    }

    override fun initEntity() {
        this.maxHealth = 10
        super.initEntity()
    }

    fun nectarDelivered(blockEntityBeehive: BlockEntityBeehive?) {
        this.setNectar(false)
    }

    fun leftBeehive(blockEntityBeehive: BlockEntityBeehive?) {
    }

    fun shouldSearchBeehive(): Boolean {
        return hasNectar() || level!!.isRaining || !level!!.isDay
    }

    override fun saveNBT() {
        super.saveNBT()
        super.namedTag!!.putBoolean("hasNectar", hasNectar)
    }

    override fun getOriginalName(): String {
        return "Bee"
    }
}
