package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import kotlin.math.abs
import kotlin.math.sqrt

class EntityXpOrb(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.XP_ORB
    }

    var closestPlayer: Player? = null
    override var age: Int = 0
    private var pickupDelay: Int = 0
    private var exp: Int = 0

    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.04f
    }

    override fun getDrag(): Float {
        return 0.02f
    }

    override fun canCollide(): Boolean {
        return false
    }

    override fun initEntity() {
        super.initEntity()

        setMaxHealth(5)
        setHealthSafe(5f)

        if (namedTag!!.contains("Health")) {
            this.setHealthSafe(namedTag!!.getShort("Health").toFloat())
        }
        if (namedTag!!.contains("Age")) {
            this.age = namedTag!!.getShort("Age").toInt()
        }
        if (namedTag!!.contains("PickupDelay")) {
            this.pickupDelay = namedTag!!.getShort("PickupDelay").toInt()
        }
        if (namedTag!!.contains("Value")) {
            this.exp = namedTag!!.getShort("Value").toInt()
        }

        if (this.exp <= 0) {
            this.exp = 1
        }

        entityDataMap.put(EntityDataTypes.VALUE, this.exp)

        //call event item spawn event
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return (source.cause == DamageCause.VOID || source.cause == DamageCause.FIRE_TICK || (source.cause == DamageCause.ENTITY_EXPLOSION ||
                source.cause == DamageCause.BLOCK_EXPLOSION) &&
                !this.isInsideOfWater()) && super.attack(source)
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

        var hasUpdate: Boolean = entityBaseTick(tickDiff)
        if (this.isAlive()) {
            if (this.pickupDelay > 0 && this.pickupDelay < 32767) { //Infinite delay
                this.pickupDelay -= tickDiff
                if (this.pickupDelay < 0) {
                    this.pickupDelay = 0
                }
            } /* else { // Done in Player#checkNearEntities
                for (Entity entity : this.level.getCollidingEntities(this.boundingBox, this)) {
                    if (entity instanceof Player) {
                        if (((Player) entity).pickupEntity(this, false)) {
                            return true;
                        }
                    }
                }
            }*/

            motion.y -= getGravity().toDouble()

            if (this.checkObstruction(
                    position.x,
                    position.y, position.z
                )
            ) {
                hasUpdate = true
            }

            if (this.closestPlayer == null || closestPlayer!!.position.distanceSquared(this.position) > 64.0) {
                this.closestPlayer = null
                var closestDistance: Double = 0.0
                for (p: Player in viewers.values) {
                    if (!p.isSpectator && p.spawned && p.isAlive()) {
                        val d: Double = p.position.distanceSquared(this.position)
                        if (d <= 64.0 && (this.closestPlayer == null || d < closestDistance)) {
                            this.closestPlayer = p
                            closestDistance = d
                        }
                    }
                }
            }

            if (this.closestPlayer != null && (closestPlayer!!.isSpectator || !closestPlayer!!.spawned || !closestPlayer!!.isAlive())) {
                this.closestPlayer = null
            }

            if (this.closestPlayer != null) {
                val dX: Double = (closestPlayer!!.position.x - position.x) / 8.0
                val dY: Double =
                    (closestPlayer!!.position.y + closestPlayer!!.getEyeHeight().toDouble() / 2.0 - position.y) / 8.0
                val dZ: Double = (closestPlayer!!.position.z - position.z) / 8.0
                val d: Double = sqrt(dX * dX + dY * dY + dZ * dZ)
                var diff: Double = 1.0 - d

                if (diff > 0.0) {
                    diff = diff * diff
                    motion.x += dX / d * diff * 0.1
                    motion.y += dY / d * diff * 0.1
                    motion.z += dZ / d * diff * 0.1
                }
            }

            this.move(motion.x, motion.y, motion.z)

            var friction: Double = 1.0 - this.getDrag()

            if (this.onGround && (abs(motion.x) > 0.00001 || abs(
                    motion.z
                ) > 0.00001)
            ) {
                friction = level!!.getBlock(position.add(0.0, -1.0, 0.0).floor()).frictionFactor * friction
            }

            motion.x *= friction
            motion.y *= (1 - this.getDrag()).toDouble()
            motion.z *= friction

            if (this.onGround) {
                motion.y *= -0.5
            }

            this.updateMovement()

            if (this.age > 6000) {
                this.kill()
                hasUpdate = true
            }
        }

        return hasUpdate || !this.onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putShort("Health", health.toInt())
        namedTag!!.putShort("Age", age)
        namedTag!!.putShort("PickupDelay", pickupDelay)
        namedTag!!.putShort("Value", exp)
    }

    fun getExp(): Int {
        return exp
    }

    fun setExp(exp: Int) {
        require(exp > 0) { "XP amount must be greater than 0, got " + exp }
        this.exp = exp
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return false
    }

    fun getPickupDelay(): Int {
        return pickupDelay
    }

    fun setPickupDelay(pickupDelay: Int) {
        this.pickupDelay = pickupDelay
    }

    override fun getOriginalName(): String {
        return "Experience Orb"
    }

    companion object {
        /**
         * Split sizes used for dropping experience orbs.
         */
        val ORB_SPLIT_SIZES: IntArray = intArrayOf(
            2477,
            1237,
            617,
            307,
            149,
            73,
            37,
            17,
            7,
            3,
            1
        ) //This is indexed biggest to smallest so that we can return as soon as we found the biggest value.

        /**
         * Returns the largest size of normal XP orb that will be spawned for the specified amount of XP. Used to split XP
         * up into multiple orbs when an amount of XP is dropped.
         */
        fun getMaxOrbSize(amount: Int): Int {
            for (split: Int in ORB_SPLIT_SIZES) {
                if (amount >= split) {
                    return split
                }
            }

            return 1
        }

        /**
         * Splits the specified amount of XP into an array of acceptable XP orb sizes.
         */
        @JvmStatic
        fun splitIntoOrbSizes(amount: Int): List<Int> {
            var amount1: Int = amount
            val result: MutableList<Int> = mutableListOf()

            while (amount1 > 0) {
                val size: Int = getMaxOrbSize(amount1)
                result.add(size)
                amount1 -= size
            }

            return result
        }
    }
}
