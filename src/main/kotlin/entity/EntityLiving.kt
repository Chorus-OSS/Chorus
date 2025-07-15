package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockCactus
import org.chorus_oss.chorus.block.BlockMagma
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.weather.EntityWeather
import org.chorus_oss.chorus.event.entity.*
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTurtleHelmet
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.round
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.network.protocol.AnimatePacket
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.scoreboard.manager.IScoreboardManager
import org.chorus_oss.chorus.utils.TickCachedBlockIterator
import java.util.*
import kotlin.math.sqrt

abstract class EntityLiving(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityDamageable {
    protected open var attackTime: Short = 0
    protected var invisible: Boolean = false
    var movementSpeed: Float = DEFAULT_SPEED
        set(value) {
            field = round(value, 2)
        }
    protected var turtleTicks: Int = 0
    private var attackTimeByShieldKb: Boolean = false
    private var attackTimeBefore: Short = 0

    override fun getGravity(): Float {
        return 0.08f
    }

    override fun getDrag(): Float {
        return 0.02f
    }

    override fun initEntity() {
        super.initEntity()

        if (namedTag!!.contains("HealF")) {
            namedTag!!.putFloat("Health", namedTag!!.getShort("HealF").toFloat())
            namedTag!!.remove("HealF")
        }

        if (!namedTag!!.contains("Health") || namedTag!!["Health"] !is FloatTag) {
            namedTag!!.putFloat("Health", getMaxHealth().toFloat())
        }

        setHealthSafe(namedTag!!.getFloat("Health"))
    }

    override fun setHealthSafe(health: Float) {
        val wasAlive: Boolean = this.isAlive()
        super.setHealthSafe(health)
        if (this.isAlive() && !wasAlive) {
            val pk = EntityEventPacket()
            pk.eid = this.getRuntimeID()
            pk.event = EntityEventPacket.RESPAWN
            Server.broadcastPacket(hasSpawned.values, pk)
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putFloat("Health", this.health)
    }

    fun hasLineOfSight(entity: Entity?): Boolean {
        //todo
        return true
    }

    fun collidingWith(ent: Entity) { // can override (IronGolem|Bats)
        ent.applyEntityCollision(this)
    }


    override fun attack(source: EntityDamageEvent): Boolean {
        if (this.noDamageTicks > 0 && source.cause != DamageCause.SUICIDE) { //ignore it if the cause is SUICIDE
            return false
        } else if (this.attackTime > 0 && !attackTimeByShieldKb) {
            val lastCause: EntityDamageEvent? = this.getLastDamageCause()
            if (lastCause != null && lastCause.damage >= source.damage) {
                return false
            }
        }

        if (isBlocking() && this.blockedByShield(source)) {
            return false
        }

        if (super.attack(source)) {
            if (source is EntityDamageByEntityEvent) {
                var damager: Entity = source.damager
                if (source is EntityDamageByChildEntityEvent) {
                    damager = source.child
                }

                //Critical hit
                if (damager is Player && !damager.onGround) {
                    val animate = AnimatePacket(
                        action = AnimatePacket.Action.CRITICAL_HIT,
                        targetRuntimeID = getRuntimeID()
                    )

                    level!!.addChunkPacket(damager.getChunkX(), damager.getChunkZ(), animate)
                    level!!.addSound(this.position, Sound.GAME_PLAYER_ATTACK_STRONG)

                    source.damage = (source.damage * 1.5f)
                }

                if (damager.isOnFire() && damager !is Player) {
                    this.setOnFire(2 * Server.instance.getDifficulty())
                }

                val deltaX: Double = position.x - damager.position.x
                val deltaZ: Double = position.z - damager.position.z
                this.knockBack(damager, source.damage.toDouble(), deltaX, deltaZ, source.knockBack.toDouble())
            }

            val pk: EntityEventPacket = EntityEventPacket()
            pk.eid = this.getRuntimeID()
            pk.event =
                if (this.health <= 0) EntityEventPacket.DEATH_ANIMATION else EntityEventPacket.HURT_ANIMATION
            Server.broadcastPacket(hasSpawned.values, pk)

            this.attackTime = source.attackCooldown
            this.attackTimeByShieldKb = false
            this.scheduleUpdate()

            return true
        } else {
            return false
        }
    }

    fun knockBack(attacker: Entity?, damage: Double, x: Double, z: Double) {
        this.knockBack(attacker, damage, x, z, 0.4)
    }

    open fun knockBack(attacker: Entity?, damage: Double, x: Double, z: Double, base: Double) {
        var f: Double = sqrt(x * x + z * z)
        if (f <= 0) {
            return
        }

        f = 1 / f

        val motion: Vector3 = Vector3(
            motion.x,
            motion.y, motion.z
        )

        motion.x /= 2.0
        motion.y /= 2.0
        motion.z /= 2.0
        motion.x += x * f * base
        motion.y += base
        motion.z += z * f * base

        if (motion.y > base) {
            motion.y = base
        }

        this.setMotion(motion)
    }

    override fun kill() {
        if (!this.isAlive()) {
            return
        }
        super.kill()
        val ev: EntityDeathEvent = EntityDeathEvent(this, this.getDrops())
        Server.instance.pluginManager.callEvent(ev)

        val manager: IScoreboardManager = Server.instance.scoreboardManager
        //测试环境中此项会null，所以说需要判空下
        manager.onEntityDead(this)

        if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
            for (item in ev.getDrops()) {
                level!!.dropItem(this.position, item)
            }
            level!!.dropExpOrb(this.position, getExperienceDrops())
        }
    }

    override fun entityBaseTick(): Boolean {
        return this.entityBaseTick(1)
    }

    override fun entityBaseTick(tickDiff: Int): Boolean {
        var isBreathing: Boolean = !this.isInsideOfWater()

        if (this is Player) {
            if (isBreathing && player.inventory.helmet is ItemTurtleHelmet) {
                turtleTicks = 200
            } else if (turtleTicks > 0) {
                isBreathing = true
                turtleTicks--
            }

            if (player.isCreative || player.isSpectator) {
                isBreathing = true
            }
        }

        this.setDataFlag(EntityFlag.BREATHING, isBreathing)

        var hasUpdate: Boolean = super.entityBaseTick(tickDiff)

        if (this.isAlive()) {
            if (this.isInsideOfSolid()) {
                hasUpdate = true
                this.attack(EntityDamageEvent(this, DamageCause.SUFFOCATION, 1f))
            }

            if (this.isOnLadder() || this.hasEffect(EffectType.LEVITATION) || this.hasEffect(EffectType.SLOW_FALLING)) {
                this.resetFallDistance()
            }

            if (!this.hasEffect(EffectType.WATER_BREATHING) && !this.hasEffect(EffectType.CONDUIT_POWER) && this.isInsideOfWater()) {
                if (this is EntitySwimmable || (this is Player && (this.isCreative || this.isSpectator))) {
                    this.setAirTicks(400)
                } else {
                    if (turtleTicks == 0 || turtleTicks == 200) {
                        hasUpdate = true
                        var airTicks: Int = this.getAirTicks() - tickDiff

                        if (airTicks <= -20) {
                            airTicks = 0
                            this.attack(EntityDamageEvent(this, DamageCause.DROWNING, 2f))
                        }

                        setAirTicks(airTicks)
                    }
                }
            } else {
                if (this is EntitySwimmable) {
                    hasUpdate = true
                    var airTicks: Int = getAirTicks() - tickDiff

                    if (airTicks <= -20) {
                        airTicks = 0
                        this.attack(EntityDamageEvent(this, DamageCause.SUFFOCATION, 2f))
                    }

                    setAirTicks(airTicks)
                } else {
                    val airTicks: Int = getAirTicks()

                    if (airTicks < 400) {
                        setAirTicks(400.coerceAtMost(airTicks + tickDiff * 5))
                    }
                }
            }
        }

        if (this.attackTime > 0) {
            this.attackTime = (this.attackTime.toInt() - tickDiff).toShort()
            if (this.attackTime <= 0) {
                attackTimeByShieldKb = false
            }
            hasUpdate = true
        }

        //吐槽：性能不要了是吧放EntityLiving这里
        //逻辑迁移到EntityVehicle去了
//        if (this.riding == null) {
//            for (Entity entity : level.fastNearbyEntities(this.boundingBox.grow(0.20000000298023224, 0.0D, 0.20000000298023224), this)) {
//                if (entity instanceof EntityRideable) {
//                    this.collidingWith(entity);
//                }
//            }
//        }

        // Used to check collisions with magma / cactus blocks
        // Math.round处理在某些条件下 出现x.999999的坐标条件,这里选择四舍五入
        val block = level!!.getTickCachedBlock(
            position.floorX, (Math.round(
                position.y
            ) - 1).toInt(),
            position.floorZ
        )
        if (block is BlockMagma || block is BlockCactus) block.onEntityCollide(this)

        return hasUpdate
    }

    /**
     * Defines the drops after the entity's death
     */
    open fun getDrops(): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    open fun getExperienceDrops(): Int {
        return 0
    }

    fun getLineOfSight(maxDistance: Int): Array<Block> {
        return this.getLineOfSight(maxDistance, 0)
    }

    fun getLineOfSight(maxDistance: Int, maxLength: Int): Array<Block> {
        return this.getLineOfSight(maxDistance, maxLength, arrayOf())
    }

    fun getLineOfSight(maxDistance: Int, maxLength: Int, transparent: Array<String?>?): Array<Block> {
        var maxDistance1: Int = maxDistance
        var transparent1: Array<String?>? = transparent
        if (maxDistance1 > 120) {
            maxDistance1 = 120
        }

        if (transparent1 != null && transparent1.isEmpty()) {
            transparent1 = null
        }

        val blocks: MutableList<Block> = ArrayList()

        val itr = TickCachedBlockIterator(
            this.level!!, this.position, this.getDirectionVector(),
            getEyeHeight().toDouble(), maxDistance1
        )

        while (itr.hasNext()) {
            val block = itr.next() ?: continue
            blocks.add(block)

            if (maxLength != 0 && blocks.size > maxLength) {
                blocks.removeAt(0)
            }

            val id: String = block.id

            if (transparent1 == null) {
                if (!block.isAir) {
                    break
                }
            } else {
                if (Arrays.binarySearch(transparent1, id) < 0) {
                    break
                }
            }
        }

        return blocks.toTypedArray()
    }

    fun getTargetBlock(maxDistance: Int): Block? {
        return getTargetBlock(maxDistance, arrayOf())
    }

    fun getTargetBlock(maxDistance: Int, transparent: Array<String?>?): Block? {
        try {
            val blocks: Array<Block> = this.getLineOfSight(maxDistance, 1, transparent)
            val block: Block = blocks[0]
            if (!transparent.isNullOrEmpty()) {
                if (Arrays.binarySearch(transparent, block.id) < 0) {
                    return block
                }
            } else {
                return block
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 设置该有生命实体的移动速度
     *
     *
     * Set the movement speed of this Entity.
     *
     * @param speed 速度大小<br></br>Speed value
     */
    open fun setMovementSpeedF(speed: Float) {
        this.movementSpeed = round(speed, 2)
    }

    fun getAirTicks(): Int {
        return getDataProperty(EntityDataTypes.AIR_SUPPLY).toInt()
    }

    fun setAirTicks(ticks: Int) {
        this.setDataProperty(EntityDataTypes.AIR_SUPPLY, ticks)
    }

    protected fun blockedByShield(source: EntityDamageEvent): Boolean {
        var damager: Entity? = null
        if (source is EntityDamageByChildEntityEvent) {
            damager = source.child
        } else if (source is EntityDamageByEntityEvent) {
            damager = source.damager
        }
        if (damager == null || damager is EntityWeather || !this.isBlocking()) {
            return false
        }

        val entityPos: Vector3 = damager.position
        val direction: Vector3 = this.getDirectionVector()
        val normalizedVector: Vector3 = position.subtract(entityPos).normalize()
        val blocked: Boolean = (normalizedVector.x * direction.x) + (normalizedVector.z * direction.z) < 0.0
        val knockBack: Boolean = damager !is EntityProjectile
        val event = EntityDamageBlockedEvent(this, source, knockBack, true)
        if (!blocked || !source.canBeReducedByArmor()) {
            event.cancelled = true
        }

        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }

        if (event.knockBackAttacker && damager is EntityLiving) {
            val deltaX: Double = damager.position.x - position.x
            val deltaZ: Double = damager.position.z - position.z
            damager.knockBack(this, 0.0, deltaX, deltaZ)
            damager.attackTime = 10
            damager.attackTimeByShieldKb = true
        }

        onBlock(damager, source, event.animation)
        return true
    }

    open fun onBlock(entity: Entity?, event: EntityDamageEvent?, animate: Boolean) {
        if (animate) {
            level!!.addSound(this.position, Sound.ITEM_SHIELD_BLOCK)
        }
    }

    fun isBlocking(): Boolean {
        return this.getDataFlag(EntityFlag.BLOCKING)
    }

    fun setBlocking(value: Boolean) {
        this.setDataFlagExtend(EntityFlag.BLOCKING, value)
    }

    open fun isPersistent(): Boolean {
        return namedTag!!.containsByte("Persistent") && namedTag!!.getBoolean("Persistent")
    }

    open fun setPersistent(persistent: Boolean) {
        namedTag!!.putBoolean("Persistent", persistent)
    }

    fun preAttack(player: Player?) {
        if (attackTimeByShieldKb) {
            attackTimeBefore = attackTime
            attackTime = 0
        }
    }

    fun postAttack(player: Player?) {
        if (attackTimeByShieldKb && attackTime == 0.toShort()) {
            attackTime = attackTimeBefore
        }
    }

    companion object {
        const val DEFAULT_SPEED: Float = 0.1f
    }
}
