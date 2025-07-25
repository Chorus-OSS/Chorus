package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.entity.item.EntityEnderCrystal
import org.chorus_oss.chorus.entity.item.EntityMinecartAbstract
import org.chorus_oss.chorus.event.entity.*
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.MovingObjectPosition
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt


abstract class EntityProjectile @JvmOverloads constructor(
    chunk: IChunk?,
    nbt: CompoundTag?,
    @JvmField var shootingEntity: Entity? = null
) :
    Entity(chunk, nbt) {
    @JvmField
    var hadCollision: Boolean = false
    var closeOnCollide: Boolean = false

    /**
     * It's inverted from [.getHasAge] because of the poor architecture chosen by the original devs
     * on the entity construction and initialization. It's impossible to set it to true before
     * the initialization of the child classes.
     */
    private var noAge: Boolean = false

    init {
        if (shootingEntity != null) {
            this.setDataProperty(EntityDataTypes.OWNER_EID, shootingEntity!!.getRuntimeID())
        }
    }

    protected open fun getDamage(): Double {
        return if (namedTag!!.contains("damage")) namedTag!!.getDouble("damage") else getBaseDamage()
    }

    protected open fun getBaseDamage(): Double {
        return 0.0
    }

    open fun getResultDamage(entity: Entity?): Int {
        return getResultDamage()
    }

    open fun getResultDamage(): Int {
        return ceil(sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z) * getDamage()).toInt()
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return source.cause == DamageCause.VOID && super.attack(source)
    }

    open fun onCollideWithEntity(entity: Entity) {
        val projectileHitEvent = ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity))
        Server.instance.pluginManager.callEvent(projectileHitEvent)

        if (projectileHitEvent.cancelled) {
            return
        }

        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this,
                this.vector3, VibrationType.PROJECTILE_LAND
            )
        )

        val damage: Float = getResultDamage(entity).toFloat()

        val ev: EntityDamageEvent
        if (this.shootingEntity == null) {
            ev = EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
        } else {
            ev = EntityDamageByChildEntityEvent(this.shootingEntity!!, this, entity, DamageCause.PROJECTILE, damage)
        }
        if (entity.attack(ev)) {
            addHitEffect()
            this.hadCollision = true

            if (this.fireTicks > 0) {
                val event: EntityCombustByEntityEvent = EntityCombustByEntityEvent(this, entity, 5)
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    entity.setOnFire(event.duration)
                }
            }
        }
        afterCollisionWithEntity(entity)
        if (closeOnCollide) {
            this.close()
        }
    }

    protected open fun afterCollisionWithEntity(entity: Entity) {
    }

    override fun initEntity() {
        this.closeOnCollide = true
        super.initEntity()

        this.setMaxHealth(1)
        this.setHealthSafe(1f)
        if (namedTag!!.contains("Age") && !this.noAge) {
            this.age = namedTag!!.getShort("Age").toInt()
        }
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return (entity is EntityLiving || entity is EntityEnderCrystal || entity is EntityMinecartAbstract || entity is EntityBoat) && !this.onGround
    }

    override fun saveNBT() {
        super.saveNBT()
        if (!this.noAge) {
            namedTag!!.putShort("Age", this.age)
        }
    }

    protected open fun updateMotion() {
        motion.y -= getGravity().toDouble()
        motion.x *= (1 - this.getDrag()).toDouble()
        motion.z *= (1 - this.getDrag()).toDouble()
    }

    /**
     * Filters the entities that collide with projectile
     *
     * @param entity the collide entity
     * @return the boolean
     */
    protected fun collideEntityFilter(entity: Entity): Boolean {
        return !((entity === this.shootingEntity && this.ticksLived < 5) ||
                (entity is Player && entity.gamemode == Player.SPECTATOR)
                || (shootingEntity is Player && Optional.ofNullable(
            shootingEntity!!.riding
        ).map { e: Entity -> e == entity }.orElse(false)))
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        val tickDiff: Int = currentTick - this.lastUpdate
        if (tickDiff <= 0 && !this.justCreated) {
            return true
        }
        this.lastUpdate = currentTick

        var hasUpdate: Boolean = this.entityBaseTick(tickDiff)

        if (this.isAlive()) {
            var movingObjectPosition: MovingObjectPosition? = null

            if (!this.isCollided) {
                updateMotion()
            }

            val moveVector: Vector3 = Vector3(
                position.x + motion.x,
                position.y + motion.y, position.z + motion.z
            )

            val list = level!!.getCollidingEntities(
                getBoundingBox()
                    .addCoord(motion.x, motion.y, motion.z).expand(1.0, 1.0, 1.0), this
            )

            var nearDistance: Double = Int.MAX_VALUE.toDouble()
            var nearEntity: Entity? = null

            for (entity: Entity in list) {
                if (!collideEntityFilter(entity)) {
                    continue
                }

                val axisalignedbb: AxisAlignedBB = entity.getBoundingBox().grow(0.3, 0.3, 0.3)
                val ob: MovingObjectPosition = axisalignedbb.calculateIntercept(this.position, moveVector) ?: continue

                val distance: Double = position.distanceSquared(ob.hitVector!!)

                if (distance < nearDistance) {
                    nearDistance = distance
                    nearEntity = entity
                }
            }

            if (nearEntity != null) {
                movingObjectPosition = MovingObjectPosition.fromEntity(nearEntity)
            }

            if (movingObjectPosition != null) {
                if (movingObjectPosition.entityHit != null) {
                    onCollideWithEntity(movingObjectPosition.entityHit!!)
                    hasUpdate = true
                    if (closed) {
                        return true
                    }
                }
            }

            val locator: Locator = locator
            val motion: Vector3 = getMotion()
            this.move(this.motion.x, this.motion.y, this.motion.z)

            if (this.isCollided && !this.hadCollision) { //collide with block
                this.hadCollision = true

                this.motion.x = 0.0
                this.motion.y = 0.0
                this.motion.z = 0.0

                Server.instance.pluginManager.callEvent(
                    ProjectileHitEvent(
                        this, MovingObjectPosition.fromBlock(
                            position.floorX,
                            position.floorY, position.floorZ, BlockFace.UP, this.position
                        )
                    )
                )
                onCollideWithBlock(locator, motion)
                addHitEffect()
                return false
            } else if (!this.isCollided && this.hadCollision) {
                this.hadCollision = false
            }

            if (!this.hadCollision || abs(this.motion.x) > 0.00001 || abs(this.motion.y) > 0.00001 || abs(
                    this.motion.z
                ) > 0.00001
            ) {
                updateRotation()
                hasUpdate = true
            }

            this.updateMovement()
        }

        return hasUpdate
    }

    fun updateRotation() {
        val f: Double = sqrt((motion.x * motion.x) + (motion.z * motion.z))
        rotation.yaw = atan2(motion.x, motion.z) * 180 / Math.PI
        rotation.pitch = atan2(motion.y, f) * 180 / Math.PI
    }

    fun inaccurate(modifier: Float) {
        val rand: Random = ThreadLocalRandom.current()

        motion.x += rand.nextGaussian() * 0.007499999832361937 * modifier
        motion.y += rand.nextGaussian() * 0.007499999832361937 * modifier
        motion.z += rand.nextGaussian() * 0.007499999832361937 * modifier
    }

    protected open fun onCollideWithBlock(locator: Locator, motion: Vector3) {
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this,
                this.vector3, VibrationType.PROJECTILE_LAND
            )
        )
        for (collisionBlock: Block in level!!.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) {
            onCollideWithBlock(locator, motion, collisionBlock)
        }
    }

    protected open fun onCollideWithBlock(locator: Locator, motion: Vector3, collisionBlock: Block): Boolean {
        return collisionBlock.onProjectileHit(this, locator, motion)
    }

    protected open fun addHitEffect() {
    }

    fun getHasAge(): Boolean {
        return !this.noAge
    }

    fun setHasAge(hasAge: Boolean) {
        this.noAge = !hasAge
    }

    override fun spawnToAll() {
        super.spawnToAll()
        //vibration: minecraft:projectile_shoot
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this.shootingEntity,
                this.vector3, VibrationType.PROJECTILE_SHOOT
            )
        )
    }

    companion object {
        const val PICKUP_NONE: Int = 0
        const val PICKUP_ANY: Int = 1
        const val PICKUP_CREATIVE: Int = 2
    }
}
