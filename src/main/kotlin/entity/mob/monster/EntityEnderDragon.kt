package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockEndGateway
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.entity.EntityFlyable
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LiftController
import org.chorus_oss.chorus.entity.ai.controller.SpaceMoveController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.RandomSoundEvaluator
import org.chorus_oss.chorus.entity.ai.executor.PlaySoundExecutor
import org.chorus_oss.chorus.entity.ai.executor.enderdragon.CircleMovementExecutor
import org.chorus_oss.chorus.entity.ai.executor.enderdragon.PerchingExecutor
import org.chorus_oss.chorus.entity.ai.executor.enderdragon.StrafeExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.FlyingPosEvaluator
import org.chorus_oss.chorus.entity.ai.route.posevaluator.IPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.item.EntityEnderCrystal
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.BossEventPacket
import org.chorus_oss.protocol.types.ActorLink
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.attribute.AttributeValue
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class EntityEnderDragon(chunk: IChunk?, nbt: CompoundTag) : EntityBoss(chunk, nbt), EntityFlyable {
    override fun getEntityIdentifier(): String {
        return EntityID.ENDER_DRAGON
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(PlaySoundExecutor(Sound.MOB_ENDERDRAGON_GROWL), RandomSoundEvaluator(), 2, 1)
            ),
            setOf<IBehavior>(
                Behavior(
                    PerchingExecutor(),
                    { entity: EntityMob? -> memoryStorage.get<Boolean>(CoreMemoryTypes.FORCE_PERCHING) },
                    5,
                    1
                ),
                Behavior(
                    StrafeExecutor(), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.LAST_ENDER_CRYSTAL_DESTROY)
                    ), 4, 1
                ),
                Behavior(
                    CircleMovementExecutor(CoreMemoryTypes.STAY_NEARBY, 1f, true, 82, 12, 5), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.STAY_NEARBY),
                        MemoryCheckEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY)
                    ), 3, 1
                ),
                Behavior(
                    CircleMovementExecutor(CoreMemoryTypes.STAY_NEARBY, 1f, true, 48, 8, 4), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.STAY_NEARBY),
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY)
                    ), 2, 1
                )
            ),
            setOf<ISensor>(
                NearestPlayerSensor(512.0, 0.0, 20),
                NearestEntitySensor(
                    EntityEnderCrystal::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    192.0,
                    0.0,
                    10
                )
            ),
            setOf<IController>(SpaceMoveController(), LookController(), LiftController()),
            EnderDragonRouteFinder(EnderDragonPosEvaluator(), this),
            this
        )
    }

    override fun createAddEntityPacket(): Packet {
        return org.chorus_oss.protocol.packets.AddActorPacket(
            actorUniqueID = this.uniqueId,
            actorRuntimeID = this.runtimeId.toULong(),
            actorType = this.getEntityIdentifier(),
            position = org.chorus_oss.protocol.types.Vector3f(this.position),
            velocity = org.chorus_oss.protocol.types.Vector3f(this.motion),
            rotation = org.chorus_oss.protocol.types.Vector2f(this.rotation),
            headYaw = this.rotation.yaw.toFloat(),
            bodyYaw = this.rotation.yaw.toFloat(),
            attributes = run {
                this.attributes.values.add(
                    Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(200f).setValue(200f)
                )
                this.attributes.values.map(AttributeValue::invoke)
            },
            actorData = ActorDataMap(this.entityDataMap),
            actorProperties = ActorProperties(this.propertySyncData()),
            actorLinks = List(passengers.size) { i ->
                ActorLink(
                    riddenActorUniqueID = this.uniqueId,
                    riderActorUniqueID = passengers[i].uniqueId,
                    type = if (i == 0) ActorLink.Companion.Type.Rider else ActorLink.Companion.Type.Passenger,
                    immediate = false,
                    riderInitiated = false,
                    vehicleAngularVelocity = 0f
                )
            }
        )
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (deathTicks != -1) return false
        when (source.cause) {
            DamageCause.SUFFOCATION, DamageCause.MAGIC -> {
                return false
            }

            else -> Unit
        }
        return super.attack(source)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        //Hack -> Ensures that Ender Dragon is always ticked.
        level!!.scheduler.scheduleTask(
            InternalPlugin.INSTANCE
        ) { this.scheduleUpdate() }
        if (deathTicks != -1) {
            if (deathTicks <= 0) {
                kill()
            } else deathTicks--
            return true
        }
        if (currentTick % 2 == 0) {
            if (currentTick % (if (position.toHorizontal().distance(Vector2.ZERO) < 1) 10 else 20) == 0) {
                level!!.addLevelSoundEvent(
                    this.position, LevelSoundEventPacket.SOUND_FLAP, -1,
                    this.getEntityIdentifier(), false, false
                )
            }
            for (e in level!!.entities.values) {
                if (e is EntityEnderCrystal) {
                    if (e.position.distance(this.position) <= 28) {
                        val health = this.health
                        if (!(health > this.maxHealth) && health != 0f) {
                            this.heal(0.2f)
                        }
                    }
                }
            }
        }
        return super.onUpdate(currentTick)
    }

    private var deathTicks = -1

    override fun kill() {
        if (deathTicks == -1) {
            deathTicks = 190
            level!!.addLevelSoundEvent(
                this.position,
                LevelSoundEventPacket.SOUND_DEATH,
                -1,
                getEntityIdentifier(),
                false,
                false
            )
            val packet = EntityEventPacket()
            packet.event = EntityEventPacket.ENDER_DRAGON_DEATH
            packet.eid = getRuntimeID()
            Server.broadcastPacket(viewers.values, packet)
            setImmobile(true)
        } else {
            super.kill()
            close()
            if (!isRevived()) {
                level!!.setBlock(
                    Vector3(0.0, (level!!.getHighestBlockAt(Vector2.ZERO) + 1).toDouble(), 0.0), Block.get(
                        BlockID.DRAGON_EGG
                    )
                )
            }
            for (i in -2..2) {
                for (j in -1..1) {
                    if (!(i == 0 && j == 0)) {
                        level!!.setBlock(Vector3(i.toDouble(), 63.0, j.toDouble()), Block.get(BlockID.END_PORTAL))
                        level!!.setBlock(Vector3(j.toDouble(), 63.0, i.toDouble()), Block.get(BlockID.END_PORTAL))
                    }
                }
            }
            for (i in 0..19) {
                val origin = Vector3.ZERO
                val angleIncrement = 360.0 / 20
                val angle = Math.toRadians(i * angleIncrement)
                val particleX = origin.x + cos(angle) * 96
                val particleZ = origin.z + sin(angle) * 96
                val dest =
                    level!!.getBlock(Vector3(particleX, 75.0, particleZ))
                if (dest !is BlockEndGateway) {
                    Arrays.stream(BlockFace.entries.toTypedArray()).forEach { face ->
                        level!!.setBlock(
                            dest.up().getSide(face).position, Block.get(
                                BlockID.BEDROCK
                            )
                        )
                    }
                    Arrays.stream(BlockFace.entries.toTypedArray()).forEach { face ->
                        level!!.setBlock(
                            dest.down().getSide(face).position, Block.get(
                                BlockID.BEDROCK
                            )
                        )
                    }
                    level!!.setBlock(dest.position, Block.get(BlockID.END_GATEWAY))
                    break
                } else continue
            }
        }
    }

    override fun getWidth(): Float {
        return 13f
    }

    override fun getHeight(): Float {
        return 4f
    }

    public override fun initEntity() {
        this.diffHandDamage = floatArrayOf(6f, 10f, 15f)
        this.maxHealth = 200
        super.initEntity()
        memoryStorage[CoreMemoryTypes.STAY_NEARBY] = Vector3(0.0, 84.0, 0.0)
        isActive = false
        noClip = true
    }

    override fun playerApplyNameTag(player: Player, item: Item): Boolean {
        return false
    }

    override fun getOriginalName(): String {
        return "Ender Dragon"
    }

    override fun isBoss(): Boolean {
        return true
    }

    override fun addBossbar(player: Player) {
        player.sendPacket(
            BossEventPacket(
                targetActorID = this.runtimeId,
                eventType = BossEventPacket.Companion.EventType.Add,
                eventData = BossEventPacket.Companion.EventType.Companion.AddData(
                    name = this.getEntityName(),
                    filteredName = this.getEntityName(),
                    color = 5u,
                    healthPercent = health / maxHealth,
                    darkenScreen = 0u,
                    overlay = 0u
                )
            )
        )
    }

    override fun getExperienceDrops(): Int {
        return if (isRevived()) 500 else 12000
    }

    private inner class EnderDragonRouteFinder(blockEvaluator: IPosEvaluator?, entity: EntityMob) :
        SimpleSpaceAStarRouteFinder(blockEvaluator, entity) {
        override fun search(): Boolean {
            val superRes = super.search()
            if (superRes && memoryStorage.notEmpty(CoreMemoryTypes.MOVE_TARGET)) {
                this.nodes = mutableListOf(nodes.first())
                nodes.add(Node(memoryStorage[CoreMemoryTypes.MOVE_TARGET]!!, null, 0, 0))
            }
            return superRes
        }
    }

    private inner class EnderDragonPosEvaluator : FlyingPosEvaluator() {
        override fun isPassable(entity: EntityMob, vector3: Vector3): Boolean {
            return true
        }
    }

    override fun move(dx: Double, dy: Double, dz: Double): Boolean {
        val superRes = super.move(dx, dy, dz)
        if (superRes) {
            level!!.getCollisionBlocks(getBoundingBox()).filter { block ->
                canBreakBlock(
                    block
                )
            }.forEach { block -> level!!.breakBlock(block) }
        }
        return superRes
    }

    fun isRevived(): Boolean {
        return if (namedTag!!.contains("Revived")) {
            namedTag!!.getBoolean("Revived")
        } else false
    }

    protected inner class LookController : IController {
        override fun control(entity: EntityMob): Boolean {
            val target = entity.memoryStorage[CoreMemoryTypes.LOOK_TARGET]!!
            val toPlayerVector = Vector3(
                entity.position.x - target.x,
                entity.position.y - target.y,
                entity.position.z - target.z
            ).normalize()
            entity.headYaw = (BVector3.getYawFromVector(toPlayerVector))
            entity.rotation.yaw = (BVector3.getYawFromVector(toPlayerVector))
            entity.rotation.pitch = (BVector3.getPitchFromVector(toPlayerVector))
            return true
        }
    }
}