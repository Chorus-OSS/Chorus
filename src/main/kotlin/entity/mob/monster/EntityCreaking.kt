package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityCreakingHeart
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.DoNothingExecutor
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.PlayerStaringSensor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelEventGenericPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class EntityCreaking(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.CREAKING
    }

    var creakingHeart: BlockEntityCreakingHeart? = null

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(DoNothingExecutor(), EntityCheckEvaluator(CoreMemoryTypes.STARING_PLAYER), 5, 1),
                Behavior(
                    PlaySoundExecutor(Sound.MOB_CREAKING_AMBIENT),
                    all(RandomSoundEvaluator(), EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET)),
                    4,
                    1
                ),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, 40, true, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    3,
                    1
                ),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, 40, false, 30),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                    2,
                    1
                ),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                PlayerStaringCreakingSensor(40.0, 70.0, true),
                NearestPlayerCreakingSensor(40.0, 0.0, 0)
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun initEntity() {
        this.maxHealth = 1
        this.diffHandDamage = floatArrayOf(2.5f, 3f, 4.5f)
        if (namedTag!!.containsCompound("creakingHeart")) {
            val tag = namedTag!!.getCompound("creakingHeart")
            val vec = Vector3(tag.getInt("x").toDouble(), tag.getInt("y").toDouble(), tag.getInt("z").toDouble())
            val block = level!!.getBlock(vec, true)
            if (block is BlockCreakingHeart) {
                block.getOrCreateBlockEntity().linkedCreaking = (this)
            }
        }
        super.initEntity()
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (source.cancelled) return false
        if (creakingHeart == null) return super.attack(source)
        if (this.isClosed() || !this.isAlive()) {
            return false
        }
        if (source is EntityDamageByEntityEvent && source.damager !is EntityCreeper) {
            memoryStorage.set(CoreMemoryTypes.ATTACK_TARGET, source.damager)
        }
        val storage = memoryStorage
        if (storage != null) {
            storage.set(CoreMemoryTypes.BE_ATTACKED_EVENT, source)
            storage.set(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, level!!.tick)
        }

        val paleLogs = Arrays.stream(
            creakingHeart!!.levelBlock.boundingBox?.let {
                level!!.getCollisionBlocks(
                    it.grow(2.0, 2.0, 2.0)
                )
            }
        ).filter { block: Block? -> block is BlockPaleOakLog }.toList().toTypedArray()
        val maxResinSpawn = ThreadLocalRandom.current().nextInt(1, 3)
        var resinSpawned = 0
        logs@ for (log in paleLogs) {
            for (face in BlockFace.entries) {
                val side = log.getSide(face)
                if (side.isAir) {
                    val clump = Block.get(BlockID.RESIN_CLUMP) as BlockResinClump
                    clump.setPropertyValue(
                        CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
                        clump.getPropertyValue(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) or (1 shl face.getOpposite().indexDUSWNE)
                    )
                    side.level.setBlock(side.position, clump)
                    resinSpawned++
                    if (resinSpawned >= maxResinSpawn) break@logs
                }
            }
        }
        return true
    }

    fun sendParticleTrail() {
        val packet = LevelEventGenericPacket()
        packet.eventId = LevelEventPacket.EVENT_PARTICLE_CREAKING_HEART_TRIAL
        val tag = CompoundTag()
        tag.putInt("CreakingAmount", 1)
        tag.putFloat("CreakingX", position.x.toFloat())
        tag.putFloat("CreakingY", position.y.toFloat())
        tag.putFloat("CreakingZ", position.z.toFloat())
        tag.putInt("HeartAmount", 1)
        tag.putFloat("HeartX", creakingHeart!!.position.x.toFloat())
        tag.putFloat("HeartY", creakingHeart!!.position.y.toFloat())
        tag.putFloat("HeartZ", creakingHeart!!.position.z.toFloat())
        packet.tag = tag
        Server.broadcastPacket(this.viewers.values, packet)
    }

    fun spawnResin() {}

    override fun kill() {
        //ToDo: Creaking Death Animation
        super.kill()
        if (creakingHeart != null && creakingHeart!!.isBlockEntityValid) {
            creakingHeart!!.linkedCreaking = (null)
        }
    }

    override fun saveNBT() {
        if (creakingHeart != null) {
            val tag = CompoundTag()
            tag.putInt("x", creakingHeart!!.position.floorX)
            tag.putInt("y", creakingHeart!!.position.floorY)
            tag.putInt("z", creakingHeart!!.position.floorZ)
            namedTag!!.putCompound("creakingHeart", tag)
        }
        super.saveNBT()
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (!(!level!!.isDay || level!!.isRaining || level!!.isThundering())) {
            this.kill()
        }
        if (creakingHeart != null) {
            if (position.distance(creakingHeart!!.position) > 32) {
                moveTarget = creakingHeart!!.position
                lookTarget = creakingHeart!!.position
            }

            if (level!!.tick - memoryStorage[CoreMemoryTypes.LAST_BE_ATTACKED_TIME] < 51) {
                sendParticleTrail()
            }
        }
        return super.onUpdate(currentTick)
    }

    override fun getHeight(): Float {
        return 2.5f
    }

    override fun getWidth(): Float {
        return 1f
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    override fun updateMovement() {
        super.updateMovement()
        if (creakingHeart != null && creakingHeart!!.isBlockEntityValid) {
            creakingHeart!!.heart.updateAroundRedstone(BlockFace.UP, BlockFace.DOWN)
        } else kill()
    }

    private inner class NearestPlayerCreakingSensor(range: Double, minRange: Double, period: Int) :
        NearestPlayerSensor(range, minRange, period) {
        override fun sense(entity: EntityMob) {
            val before = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_PLAYER)
            super.sense(entity)
            val after = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_PLAYER)
            if (before !== after) {
                if (before == null) {
                    entity.level!!.addSound(entity.position, Sound.MOB_CREAKING_ACTIVATE)
                } else if (after == null) {
                    entity.level!!.addSound(entity.position, Sound.MOB_CREAKING_DEACTIVATE)
                }
            }
        }
    }

    private inner class PlayerStaringCreakingSensor(range: Double, triggerDiff: Double, ignoreRotation: Boolean) :
        PlayerStaringSensor(range, triggerDiff, ignoreRotation) {
        override fun sense(entity: EntityMob) {
            val before = entity.memoryStorage.get(CoreMemoryTypes.STARING_PLAYER)
            super.sense(entity)
            val after = entity.memoryStorage.get(CoreMemoryTypes.STARING_PLAYER)
            if (before !== after) {
                if (before == null) {
                    entity.level!!.addSound(entity.position, Sound.MOB_CREAKING_FREEZE)
                } else if (after == null) {
                    entity.level!!.addSound(entity.position, Sound.MOB_CREAKING_UNFREEZE)
                }
            }
        }
    }
}
