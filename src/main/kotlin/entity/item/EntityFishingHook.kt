package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.projectile.SlenderProjectile
import org.chorus_oss.chorus.event.entity.EntityDamageByChildEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.ProjectileHitEvent
import org.chorus_oss.chorus.event.player.PlayerFishEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.randomitem.Fishing
import org.chorus_oss.chorus.level.MovingObjectPosition
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.BubbleParticle
import org.chorus_oss.chorus.level.particle.WaterParticle
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.ActorLink
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.attribute.AttributeValue
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.sqrt

class EntityFishingHook @JvmOverloads constructor(chunk: IChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) :
    SlenderProjectile(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.FISHING_HOOK
    }

    var waitChance: Int = 120
    var waitTimer: Int = 240
    var attracted: Boolean = false
    var attractTimer: Int = 0
    var caught: Boolean = false
    var caughtTimer: Int = 0
    var canCollide: Boolean = true

    var fish: Vector3? = null

    @JvmField
    var rod: Item? = null

    init {
        // https://github.com/PowerNukkit/PowerNukkit/issues/267
        if (this.age > 0) {
            this.close()
        }
    }

    override fun getLength(): Float {
        return 0.2f
    }

    override fun getGravity(): Float {
        return 0.05f
    }

    public override fun getDrag(): Float {
        return 0.04f
    }

    override fun canCollide(): Boolean {
        return this.canCollide
    }

    override fun onUpdate(currentTick: Int): Boolean {
        var hasUpdate: Boolean
        val target: Long = getDataProperty<Long>(EntityDataTypes.TARGET_EID)
        if (target != 0L) {
            val entity: Entity? = level!!.getEntity(target)
            if (entity == null || !entity.isAlive()) {
                setDataProperty(EntityDataTypes.TARGET_EID, 0L)
            } else {
                val offset: Vector3f = entity.getMountedOffset(this)
                setPosition(
                    Vector3(
                        entity.position.x + offset.x,
                        entity.position.y + offset.y,
                        entity.position.z + offset.z
                    )
                )
            }
            return false
        }

        hasUpdate = super.onUpdate(currentTick)

        val inWater: Boolean = this.isInsideOfWater()
        if (inWater) { //防止鱼钩沉底 水中的阻力
            motion.x = 0.0
            motion.y -= getGravity() * -0.04
            motion.z = 0.0
            hasUpdate = true
        }

        if (inWater) {
            if (this.waitTimer == 240) {
                this.waitTimer = this.waitChance shl 1
            } else if (this.waitTimer == 360) {
                this.waitTimer = this.waitChance * 3
            }
            if (!this.attracted) {
                if (this.waitTimer > 0) {
                    --this.waitTimer
                }
                if (this.waitTimer == 0) {
                    val random: ThreadLocalRandom = ThreadLocalRandom.current()
                    if (random.nextInt(100) < 90) {
                        this.attractTimer = (random.nextInt(40) + 20)
                        this.spawnFish()
                        this.caught = false
                        this.attracted = true
                    } else {
                        this.waitTimer = this.waitChance
                    }
                }
            } else if (!this.caught) {
                if (this.attractFish()) {
                    this.caughtTimer = (ThreadLocalRandom.current().nextInt(20) + 30)
                    this.fishBites()
                    this.caught = true
                }
            } else {
                if (this.caughtTimer > 0) {
                    --this.caughtTimer
                }
                if (this.caughtTimer == 0) {
                    this.attracted = false
                    this.caught = false
                    this.waitTimer = this.waitChance * 3
                }
            }
        }
        return hasUpdate
    }

    override fun updateMotion() {
        //正确的浮力
        if (this.isInsideOfWater() && this.getY() < this.getWaterHeight() - 2) {
            motion.x = 0.0
            motion.y += getGravity().toDouble()
            motion.z = 0.0
        } else if (this.isInsideOfWater() && this.getY() >= this.getWaterHeight() - 2) { //防止鱼钩上浮超出水面
            motion.x = 0.0
            motion.z = 0.0
            motion.y = 0.0
        } else { //处理不在水中的情况
            super.updateMotion()
        }
    }

    fun getWaterHeight(): Int {
        for (y in position.floorY..level!!.maxHeight) {
            val id: String = level!!.getBlockIdAt(position.floorX, y, position.floorZ)
            if (id == BlockID.AIR) {
                return y
            }
        }
        return position.floorY
    }

    fun fishBites() {
        val viewers: Collection<Player> = viewers.values

        val pk: EntityEventPacket = EntityEventPacket()
        pk.eid = this.getRuntimeID()
        pk.event = EntityEventPacket.FISH_HOOK_HOOK
        Server.broadcastPacket(viewers, pk)

        val bubblePk: EntityEventPacket = EntityEventPacket()
        bubblePk.eid = this.getRuntimeID()
        bubblePk.event = EntityEventPacket.FISH_HOOK_BUBBLE
        Server.broadcastPacket(viewers, bubblePk)

        val teasePk: EntityEventPacket = EntityEventPacket()
        teasePk.eid = this.getRuntimeID()
        teasePk.event = EntityEventPacket.FISH_HOOK_TEASE
        Server.broadcastPacket(viewers, teasePk)

        val random: ThreadLocalRandom = ThreadLocalRandom.current()
        for (i in 0..4) {
            level!!.addParticle(
                BubbleParticle(
                    position.setComponents(
                        position.x + random.nextDouble() * 0.5 - 0.25,
                        getWaterHeight().toDouble(),
                        position.z + random.nextDouble() * 0.5 - 0.25
                    )
                )
            )
        }
    }

    fun spawnFish() {
        val random: ThreadLocalRandom = ThreadLocalRandom.current()
        this.fish = Vector3(
            position.x + (random.nextDouble() * 1.2 + 1) * (if (random.nextBoolean()) -1 else 1),
            getWaterHeight().toDouble(),
            position.z + (random.nextDouble() * 1.2 + 1) * (if (random.nextBoolean()) -1 else 1)
        )
    }

    fun attractFish(): Boolean {
        val multiply = 0.1
        fish!!.setComponents(
            fish!!.x + (position.x - fish!!.x) * multiply,
            fish!!.y,
            fish!!.z + (position.z - fish!!.z) * multiply
        )
        if (ThreadLocalRandom.current().nextInt(100) < 85) {
            level!!.addParticle(WaterParticle(this.fish!!))
        }
        val dist: Double = abs(
            sqrt(position.x * position.x + position.z * position.z) - sqrt(
                fish!!.x * fish!!.x + fish!!.z * fish!!.z
            )
        )
        return dist < 0.15
    }

    fun reelLine() {
        if (shootingEntity is Player && this.caught) {
            val player = shootingEntity as Player
            val item: Item = Fishing.getFishingResult(this.rod)
            val experience: Int = ThreadLocalRandom.current().nextInt(3) + 1
            val pos: Vector3 = Vector3(
                position.x,
                getWaterHeight().toDouble(), position.z
            ) //实体生成在水面上
            val motion: Vector3 = player.position.subtract(pos).multiply(0.1)
            motion.y += sqrt(player.position.distance(pos)) * 0.08

            val event = PlayerFishEvent(player, this, item, experience, motion)
            Server.instance.pluginManager.callEvent(event)

            if (!event.cancelled) {
                val itemEntity: EntityItem? = createEntity(
                    EntityID.ITEM,
                    level!!.getChunk(
                        position.x.toInt() shr 4,
                        position.z.toInt() shr 4, true
                    ),
                    getDefaultNBT(
                        pos,
                        event.motion, ThreadLocalRandom.current().nextFloat() * 360,
                        0f
                    ).putCompound("Item", NBTIO.putItemHelper(event.loot))
                        .putShort("Health", 5)
                        .putShort("PickupDelay", 1)
                ) as EntityItem?

                if (itemEntity != null) {
                    itemEntity.owner = (player.getEntityName())
                    itemEntity.spawnToAll()
                    player.level!!.dropExpOrb(player.position, event.experience)
                }
            }
        } else if (this.shootingEntity != null) {
            val eid: Long = this.getDataProperty<Long>(EntityDataTypes.TARGET_EID)
            val targetEntity: Entity? = level!!.getEntity(eid)
            if (targetEntity != null && targetEntity.isAlive()) { // 钓鱼竿收杆应该牵引被钓生物
                targetEntity.setMotion(
                    shootingEntity!!.position.subtract(targetEntity.position).divide(8.0).add(0.0, 0.3, 0.0)
                )
            }
        }
        this.close()
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
            attributes = this.attributes.values.map(AttributeValue::invoke),
            actorData = run {
                this.entityDataMap[EntityDataTypes.OWNER_EID] = shootingEntity?.getRuntimeID() ?: -1
                ActorDataMap(this.entityDataMap)
            },
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


    override fun onCollideWithEntity(entity: Entity) {
        Server.instance.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)))
        val damage: Float = getResultDamage().toFloat()
        val ev: EntityDamageEvent = if (this.shootingEntity == null) {
            EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
        } else {
            EntityDamageByChildEntityEvent(this.shootingEntity!!, this, entity, DamageCause.PROJECTILE, damage)
        }

        if (entity.attack(ev)) {
            this.setTarget(entity.getRuntimeID())
        }
    }

    fun checkLure() {
        if (rod != null) {
            val ench: Enchantment? = rod!!.getEnchantment(Enchantment.ID_LURE)
            if (ench != null) {
                this.waitChance = 120 - (25 * ench.level)
            }
        }
    }

    fun setTarget(eid: Long) {
        this.setDataProperty(EntityDataTypes.TARGET_EID, eid)
        this.canCollide = eid == 0L
    }

    override fun getOriginalName(): String {
        return "Fishing Hook"
    }
}
