package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.blockentity.BlockEntityPistonArm
import org.chorus_oss.chorus.entity.data.*
import org.chorus_oss.chorus.entity.data.property.*
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.mob.EntityArmorStand
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityBoss
import org.chorus_oss.chorus.entity.mob.monster.EntityEnderDragon
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.block.FarmLandDecayEvent
import org.chorus_oss.chorus.event.entity.*
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.event.entity.EntityPortalEnterEvent.PortalType
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent.TeleportCause
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTotemOfUndying
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.*
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.ExplodeParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.*
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.network.protocol.*
import org.chorus_oss.chorus.network.protocol.types.PropertySyncData
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.scheduler.Task
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.utils.*
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.SetActorDataPacket
import org.chorus_oss.protocol.packets.SetActorMotionPacket
import org.chorus_oss.protocol.types.ActorLink
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.attribute.AttributeValue
import java.awt.Color
import java.util.*
import java.util.Objects.requireNonNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.Volatile
import kotlin.math.*
import kotlin.random.Random

abstract class Entity(chunk: IChunk?, nbt: CompoundTag?) : IVector3 {
    var chested: Boolean = false
    open var color: Byte = 0
    open var color2: Byte = 0
    var customName: String? = null
    var customNameVisible: Boolean? = null
    var definitions: ListTag<StringTag>? = null
    var fallDistance: Float = 0.0f
    var fire: Short = 0
    var identifier: String = ""
    var internalComponents: CompoundTag = CompoundTag()

    @JvmField
    var invulnerable: Boolean = false

    @JvmField
    var isAngry: Boolean = false

    @JvmField
    var isAutonomous: Boolean = false

    @JvmField
    var isBaby: Boolean = false

    @JvmField
    var isEating: Boolean = false

    @JvmField
    var isGliding: Boolean = false

    @JvmField
    var isGlobal: Boolean = false

    @JvmField
    var isIllagerCaptain: Boolean = false

    @JvmField
    var isOrphaned: Boolean = false

    @JvmField
    var isOutOfControl: Boolean = false

    @JvmField
    var isRoaring: Boolean = false

    @JvmField
    var isScared: Boolean = false

    @JvmField
    var isStunned: Boolean = false

    @JvmField
    var isSwimming: Boolean = false

    @JvmField
    var isTamed: Boolean = false

    @JvmField
    var isTrusting: Boolean = false

    @JvmField
    var lastDimensionId: Int? = null

    @JvmField
    var linksTag: CompoundTag? = null

    @JvmField
    var lootDropped: Boolean = true

    @JvmField
    var markVariant: Int = 0

    @JvmField
    var onGround: Boolean = true

    @JvmField
    var ownerNew: Long = -1L

    @JvmField
    var persistent: Boolean = false

    @JvmField
    var portalCooldown: Int = 0

    @JvmField
    var saddled: Boolean = false
    open var sheared: Boolean = false

    @JvmField
    var showBottom: Boolean = false

    @JvmField
    var sitting: Boolean = false

    @JvmField
    var skinId: Int = 0

    @JvmField
    var strength: Int = 0

    @JvmField
    var strengthMax: Int = 0

    @JvmField
    var tags: ListTag<StringTag>? = null

    @JvmField
    var uniqueId: Long = 0L
    open var variant: Int = 0

    @JvmField
    var level: Level? = null

    @JvmField
    var position: Vector3 = Vector3()

    @JvmField
    var rotation: Rotator2 = Rotator2()

    @JvmField
    var motion: Vector3 = Vector3()

    @JvmField
    var prevPosition: Vector3 = position.clone()

    @JvmField
    var prevRotation: Rotator2 = rotation.clone()
    var prevMotion: Vector3 = motion.clone()

    @JvmField
    protected val entityDataMap: EntityDataMap = EntityDataMap()
    val passengers: MutableList<Entity> = ArrayList()
    val offsetBoundingBox: AxisAlignedBB = SimpleAxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    @JvmField
    protected val hasSpawned: MutableMap<Int, Player> = ConcurrentHashMap()

    @JvmField
    protected val effects: MutableMap<EffectType, Effect> = ConcurrentHashMap()

    /**
     * 这个实体骑在谁身上
     *
     *
     * Who is this entity riding on
     */
    @JvmField
    var riding: Entity? = null

    @JvmField
    var chunk: IChunk? = null

    @JvmField
    var blocksAround: MutableList<Block>? = ArrayList()

    @JvmField
    var collisionBlocks: MutableList<Block>? = ArrayList()

    @JvmField
    var firstMove: Boolean = true
    var deadTicks: Int = 0

    var entityCollisionReduction: Double = 0.0 // Higher than 0.9 will result a fast collisions

    @JvmField
    var boundingBox: AxisAlignedBB = SimpleAxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    @JvmField
    var positionChanged: Boolean = false

    @JvmField
    var motionChanged: Boolean = false

    @JvmField
    var ticksLived: Int = 0

    @JvmField
    var lastUpdate: Int = 0

    @JvmField
    var fireTicks: Int = 0

    @JvmField
    var inPortalTicks: Int = 0

    @JvmField
    var freezingTicks: Int = 0 //0 - 140

    @JvmField
    var scale: Float = 1f

    @JvmField
    var namedTag: CompoundTag = CompoundTag()

    @JvmField
    var isCollided: Boolean = false
    var isCollidedHorizontally: Boolean = false
    var isCollidedVertically: Boolean = false

    @JvmField
    var noDamageTicks: Int = 0

    @JvmField
    var justCreated: Boolean = false
    var fireProof: Boolean = false

    @JvmField
    var highestPosition: Double = 0.0

    @JvmField
    var closed: Boolean = false

    @JvmField
    var noClip: Boolean = false


    @Volatile
    protected var runtimeId: Long = 0

    @JvmField
    protected var lastDamageCause: EntityDamageEvent? = null

    protected open var age: Int = 0

    var health: Float = 20f
        protected set

    @JvmField
    protected var absorption: Float = 0f

    /**
     * Player do not use
     */
    protected var ySize: Float = 0f

    @JvmField
    protected var inEndPortal: Boolean = false
    val isPlayer: Boolean = this is Player

    @JvmField
    var maxHealth: Int = 20
    protected var idName: String? = null

    @Volatile
    private var initialized: Boolean = false

    @Volatile
    protected var saveWithChunk: Boolean = true
    private val intProperties: MutableMap<String, Int> = LinkedHashMap()
    private val floatProperties: MutableMap<String, Float> = LinkedHashMap()

    @JvmField
    protected val attributes: MutableMap<Int, Attribute> = HashMap()

    private fun idConvertToName(): String {
        val path: String = getEntityIdentifier().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val result: StringBuilder = StringBuilder()
        val parts: Array<String> = path.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (part: String in parts) {
            if (part.isNotEmpty()) {
                result.append(part[0].uppercaseChar()).append(part.substring(1)).append(" ")
            }
        }
        this.idName = result.toString().trim { it <= ' ' }.intern()
        return idName ?: ""
    }

    init {
        initEntityProperties(this.getEntityIdentifier())

        if (chunk != null) {
            this.level = chunk.provider.level
        }

        if (this !is Player) {
            this.init(chunk!!, nbt)
        }
    }

    /**
     * 获取该实体的标识符
     *
     *
     * Get the identifier of the entity
     *
     * @return the identifier
     */
    abstract fun getEntityIdentifier(): String

    /**
     * 实体高度
     *
     *
     * entity Height
     *
     * @return the height
     */
    open fun getHeight(): Float {
        return 0f
    }


    fun getCurrentHeight(): Float {
        return if (isSwimming()) {
            getSwimmingHeight()
        } else {
            getHeight()
        }
    }

    open fun getEyeHeight(): Float {
        return getCurrentHeight() / 2 + 0.1f
    }

    open fun getWidth(): Float {
        return 0f
    }

    open fun getLength(): Float {
        return 0f
    }

    protected open fun getStepHeight(): Double {
        return 0.0
    }

    open fun canCollide(): Boolean {
        return true
    }

    open fun getGravity(): Float {
        return 0f
    }

    protected open fun getDrag(): Float {
        return 0f
    }

    protected open fun getBaseOffset(): Float {
        return 0f
    }

    open fun getFrostbiteInjury(): Int {
        return 1
    }

    /**
     * 实体初始化顺序，先初始化Entity类字段->Entity构造函数->进入init方法->调用initEntity方法->子类字段初始化->子类构造函数
     *
     *
     * 用于初始化实体的NBT和实体字段的方法
     *
     *
     * Entity initialization order, first initialize the Entity class field->Entity constructor->Enter the init method->Call the init Entity method-> subclass field initialization-> subclass constructor
     *
     *
     * The method used to initialize the NBT and entity fields of the entity
     */
    protected open fun initEntity() {
        if (this !is Player) {
            if (namedTag.contains(TAG_UNIQUE_ID)) {
                this.uniqueId = namedTag.getLong(TAG_UNIQUE_ID)
            } else {
                this.uniqueId = Random.nextLong()
            }
        }

        if (namedTag!!.contains(TAG_CUSTOM_NAME)) {
            this.setNameTag(namedTag!!.getString(TAG_CUSTOM_NAME))
        }
        if (namedTag!!.contains(TAG_CUSTOM_NAME_VISIBLE)) {
            this.setNameTagAlwaysVisible(namedTag!!.getBoolean(TAG_CUSTOM_NAME_VISIBLE))
        }

        entityDataMap.getOrCreateFlags()
        entityDataMap.put(EntityDataTypes.AIR_SUPPLY, namedTag!!.getShort("Air"))
        entityDataMap.put(EntityDataTypes.AIR_SUPPLY_MAX, 400)
        entityDataMap.put(EntityDataTypes.NAME, "")
        entityDataMap.put(EntityDataTypes.LEASH_HOLDER, -1)
        entityDataMap.put(EntityDataTypes.SCALE, 1f)
        entityDataMap.put(EntityDataTypes.HEIGHT, this.getHeight())
        entityDataMap.put(EntityDataTypes.WIDTH, this.getWidth())
        entityDataMap.put(EntityDataTypes.STRUCTURAL_INTEGRITY, health.toInt())
        entityDataMap.put(EntityDataTypes.VARIANT, this.variant)
        this.sendData(hasSpawned.values.toTypedArray(), entityDataMap)
        this.setDataFlags(
            EnumSet.of(
                EntityFlag.CAN_CLIMB,
                EntityFlag.BREATHING,
                EntityFlag.HAS_COLLISION,
                EntityFlag.HAS_GRAVITY
            )
        )
    }

    protected fun init(chunk: IChunk, nbt: CompoundTag?) {
        if (chunk.provider.level == null) {
            throw ChunkException("Invalid garbage Chunk given to Entity")
        }
        this.runtimeId = entityCount.getAndIncrement()
        this.justCreated = true
        this.namedTag = nbt ?: CompoundTag()
        this.chunk = chunk
        this.level = (chunk.provider.level)

        this.chested = namedTag.getBoolean(TAG_CHESTED)
        this.color = namedTag!!.getByte(TAG_COLOR)
        this.color2 = namedTag!!.getByte(TAG_COLOR2)
        this.customName = if (namedTag!!.contains(TAG_CUSTOM_NAME)) namedTag!!.getString(TAG_CUSTOM_NAME) else null
        this.customNameVisible = namedTag!!.getBoolean(TAG_CUSTOM_NAME_VISIBLE)


        val posList: ListTag<FloatTag> = namedTag!!.getList(
            TAG_POS,
            FloatTag::class.java
        )
        val rotationList: ListTag<FloatTag> = namedTag!!.getList(
            TAG_ROTATION,
            FloatTag::class.java
        )

        this.setPositionAndRotation(
            Vector3(
                posList.get(0).data.toDouble(),
                posList.get(1).data.toDouble(),
                posList.get(2).data.toDouble()
            ),
            rotationList.get(0).data.toDouble(),
            rotationList.get(1).data.toDouble()
        )

        if (namedTag!!.contains(TAG_MOTION)) {
            val motionList: ListTag<FloatTag> = namedTag!!.getList(
                TAG_MOTION,
                FloatTag::class.java
            )

            this.setMotion(
                Vector3(
                    motionList.get(0).data.toDouble(),
                    motionList.get(1).data.toDouble(),
                    motionList.get(2).data.toDouble()
                )
            )
        }

        if (!namedTag!!.contains(TAG_FALL_DISTANCE)) {
            namedTag!!.putFloat(TAG_FALL_DISTANCE, 0f)
        }
        this.fallDistance = namedTag!!.getFloat(TAG_FALL_DISTANCE)
        this.highestPosition = position.y + namedTag!!.getFloat(TAG_FALL_DISTANCE)

        if (!namedTag!!.contains(TAG_FIRE)) {
            namedTag!!.putShort(TAG_FIRE, 0)
        }
        this.fireTicks =
            namedTag!!.getShort(TAG_FIRE).toInt()

        if (!namedTag!!.contains(TAG_ON_GROUND)) {
            namedTag!!.putBoolean(TAG_ON_GROUND, false)
        }
        this.onGround = namedTag!!.getBoolean(TAG_ON_GROUND)

        if (!namedTag!!.contains(TAG_INVULNERABLE)) {
            namedTag!!.putBoolean(TAG_INVULNERABLE, false)
        }
        this.invulnerable = namedTag!!.getBoolean(TAG_INVULNERABLE)

        this.variant = namedTag!!.getInt(TAG_VARIANT)

        try {
            this.initEntity()
            if (this.initialized) return

            this.initialized = true

            this.chunk!!.addEntity(this)
            level!!.addEntity(this)

            val event = EntitySpawnEvent(this)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                this.close(false)
            } else {
                this.scheduleUpdate()
                this.lastUpdate = level!!.tick
            }
        } catch (e: Exception) {
            this.close(false)
            throw e
        }
    }

    fun hasCustomName(): Boolean {
        return getNameTag().isNotEmpty()
    }

    fun getNameTag(): String {
        return this.getDataProperty(EntityDataTypes.NAME, "")
    }

    fun setNameTag(name: String) {
        this.setDataProperty(EntityDataTypes.NAME, name)
    }

    fun isNameTagVisible(): Boolean {
        return this.getDataFlag(EntityFlag.CAN_SHOW_NAME)
    }

    fun setNameTagVisible(value: Boolean) {
        this.setDataFlag(EntityFlag.CAN_SHOW_NAME, value)
    }

    fun isNameTagAlwaysVisible(): Boolean {
        return getDataProperty(EntityDataTypes.NAMETAG_ALWAYS_SHOW, 0.toByte()).toInt() == 1
    }

    fun setNameTagAlwaysVisible(value: Boolean) {
        this.setDataProperty(EntityDataTypes.NAMETAG_ALWAYS_SHOW, if (value) 1 else 0)
    }

    fun getScoreTag(): String {
        return this.getDataProperty(EntityDataTypes.SCORE, "")
    }

    fun setScoreTag(score: String) {
        this.setDataProperty(EntityDataTypes.SCORE, score)
    }

    fun isSneaking(): Boolean {
        return this.getDataFlag(EntityFlag.SNEAKING)
    }

    fun setSneaking(value: Boolean) {
        this.setDataFlag(EntityFlag.SNEAKING, value)
    }

    fun isSwimming(): Boolean {
        return this.getDataFlag(EntityFlag.SWIMMING)
    }

    open fun setSwimming(value: Boolean) {
        if (isSwimming() == value) {
            return
        }
        this.setDataFlag(EntityFlag.SWIMMING, value)
        if (getSwimmingHeight().compareTo(getHeight()) != 0) {
            recalculateBoundingBox(true)
        }
    }

    fun isSprinting(): Boolean {
        return this.getDataFlag(EntityFlag.SPRINTING)
    }

    open fun setSprinting(value: Boolean) {
        this.setDataFlag(EntityFlag.SPRINTING, value)
    }

    fun isGliding(): Boolean {
        return this.getDataFlag(EntityFlag.GLIDING)
    }

    fun setGliding(value: Boolean) {
        this.setDataFlag(EntityFlag.GLIDING, value)
    }

    fun isImmobile(): Boolean {
        return this.getDataFlag(EntityFlag.NO_AI)
    }

    fun setImmobile(value: Boolean) {
        this.setDataFlag(EntityFlag.NO_AI, value)
    }

    fun canClimb(): Boolean {
        return this.getDataFlag(EntityFlag.CAN_CLIMB)
    }

    fun setCanClimb(value: Boolean) {
        this.setDataFlag(EntityFlag.CAN_CLIMB, value)
    }

    fun canClimbWalls(): Boolean {
        return this.getDataFlag(EntityFlag.WALL_CLIMBING)
    }

    fun setCanClimbWalls(value: Boolean) {
        this.setDataFlag(EntityFlag.WALL_CLIMBING, value)
    }

    fun getScale(): Float {
        return this.scale
    }

    fun setScale(scale: Float) {
        this.scale = scale
        this.setDataProperty(EntityDataTypes.SCALE, this.scale)
        this.recalculateBoundingBox()
    }

    open fun getSwimmingHeight(): Float {
        return getHeight()
    }

    fun getPassenger(): Entity? {
        return this.passengers.firstOrNull()
    }

    fun isPassenger(entity: Entity?): Boolean {
        return passengers.contains(entity)
    }

    open fun isControlling(entity: Entity?): Boolean {
        return passengers.indexOf(entity) == 0
    }

    fun hasControllingPassenger(): Boolean {
        return passengers.isNotEmpty() && isControlling(passengers.first())
    }

    fun getRiding(): Entity? {
        return riding
    }

    fun getEffects(): Map<EffectType, Effect> {
        return effects
    }

    fun removeAllEffects() {
        for (effect: Effect in effects.values) {
            this.removeEffect(effect.getType())
        }
    }

    fun removeEffect(type: EffectType?) {
        if (effects.containsKey(type)) {
            val effect: Effect = effects[type]!!

            val event = EntityEffectRemoveEvent(this, effect)
            Server.instance.pluginManager.callEvent(event)

            if (event.cancelled) {
                return
            }

            if (this is Player) {
                val packet = MobEffectPacket()
                packet.eid = player.getRuntimeID()
                packet.effectId = effect.getId()
                packet.eventId = MobEffectPacket.EVENT_REMOVE.toInt()
                player.dataPacket(packet)
            }

            effect.remove(this)
            effects.remove(type)

            this.recalculateEffectColor()
        }
    }

    fun getEffect(type: EffectType): Effect? {
        return effects.getOrDefault(type, null)
    }

    fun hasEffect(type: EffectType): Boolean {
        return effects.containsKey(type)
    }

    fun addEffect(effect: Effect) {
        val oldEffect: Effect? = this.getEffect(effect.getType())

        if (oldEffect != null) {
            val event = EntityEffectUpdateEvent(this, oldEffect, effect)
            Server.instance.pluginManager.callEvent(event)

            if (event.cancelled) {
                return
            }
        }

        if (oldEffect != null && (abs(effect.getAmplifier().toDouble()) < abs(oldEffect.getAmplifier().toDouble()) ||
                    (abs(effect.getAmplifier().toDouble()) == abs(oldEffect.getAmplifier().toDouble()) &&
                            effect.getDuration() < oldEffect.getDuration())
                    )
        ) {
            return
        }

        if (this is Player) {
            val packet = MobEffectPacket()
            packet.eid = player.getRuntimeID()
            packet.effectId = effect.getId()
            packet.amplifier = effect.getAmplifier()
            packet.particles = effect.isVisible()
            packet.duration = effect.getDuration()
            if (oldEffect != null) {
                packet.eventId = MobEffectPacket.EVENT_MODIFY.toInt()
            } else {
                packet.eventId = MobEffectPacket.EVENT_ADD.toInt()
            }

            player.dataPacket(packet)
        }

        effect.add(this)
        effects[effect.getType()] = effect

        this.recalculateEffectColor()
    }

    @JvmOverloads
    fun recalculateBoundingBox(send: Boolean = true) {
        val entityHeight: Float = getCurrentHeight()
        val height: Float = entityHeight * this.scale
        val radius: Double = (this.getWidth() * this.scale) / 2.0
        boundingBox.setBounds(
            position.x - radius,
            position.y,
            position.z - radius,

            position.x + radius,
            position.y + height,
            position.z + radius
        )

        var change = false
        if (getEntityDataMap().get(EntityDataTypes.HEIGHT) != entityHeight) {
            change = true
            getEntityDataMap().put(EntityDataTypes.HEIGHT, entityHeight)
        }
        if (getEntityDataMap().get(EntityDataTypes.WIDTH) != this.getWidth()) {
            change = true
            getEntityDataMap().put(EntityDataTypes.WIDTH, this.getWidth())
        }
        if (send && change) {
            sendData(
                hasSpawned.values.toTypedArray(),
                entityDataMap.copy(EntityDataTypes.WIDTH, EntityDataTypes.HEIGHT)
            )
        }
    }

    protected fun recalculateEffectColor() {
        val color = IntArray(3)
        var count = 0
        var ambient = true
        for (effect: Effect in effects.values) {
            if (effect.isVisible()) {
                val effectColor: Color = effect.getColor()
                color[0] += effectColor.red * effect.getLevel()
                color[1] += effectColor.green * effect.getLevel()
                color[2] += effectColor.blue * effect.getLevel()
                count += effect.getLevel()
                if (!effect.isAmbient()) {
                    ambient = false
                }
            }
        }

        if (count > 0) {
            val r: Int = (color[0] / count) and 0xff
            val g: Int = (color[1] / count) and 0xff
            val b: Int = (color[2] / count) and 0xff
            setDataProperties(
                mapOf<EntityDataType<*>, Any>(
                    Pair(EntityDataTypes.EFFECT_COLOR, (r shl 16) + (g shl 8) + b),
                    Pair(EntityDataTypes.EFFECT_AMBIENCE, if (ambient) 1 else 0)
                )
            )
        } else {
            setDataProperties(
                mapOf<EntityDataType<*>, Any>(
                    Pair(EntityDataTypes.EFFECT_COLOR, 0),
                    Pair(EntityDataTypes.EFFECT_AMBIENCE, 0)
                )
            )
        }
    }

    open fun saveNBT() {
        if (this !is Player) {
            namedTag!!.putString(TAG_IDENTIFIER, this.getEntityIdentifier())
            if (getNameTag().isNotEmpty()) {
                namedTag!!.putString(TAG_CUSTOM_NAME, this.getNameTag())
                namedTag!!.putBoolean(TAG_CUSTOM_NAME_VISIBLE, this.isNameTagAlwaysVisible())
            } else {
                namedTag!!.remove(TAG_CUSTOM_NAME)
                namedTag!!.remove(TAG_CUSTOM_NAME_VISIBLE)
            }
            namedTag!!.putLong(
                TAG_UNIQUE_ID,
                this.uniqueId
            )
        }

        namedTag!!.putList(
            TAG_POS, ListTag<FloatTag>()
                .add(FloatTag(position.x.toFloat()))
                .add(FloatTag(position.y.toFloat()))
                .add(FloatTag(position.z.toFloat()))
        )

        namedTag!!.putList(
            TAG_MOTION, ListTag<FloatTag>()
                .add(FloatTag(motion.x.toFloat()))
                .add(FloatTag(motion.y.toFloat()))
                .add(FloatTag(motion.z.toFloat()))
        )

        namedTag!!.putList(
            TAG_ROTATION, ListTag<FloatTag>()
                .add(FloatTag(rotation.yaw.toFloat()))
                .add(FloatTag(rotation.pitch.toFloat()))
        )

        namedTag!!.putFloat(TAG_FALL_DISTANCE, this.fallDistance)
        namedTag!!.putShort(TAG_FIRE, this.fireTicks)
        namedTag!!.putBoolean(TAG_ON_GROUND, this.onGround)
        namedTag!!.putBoolean(TAG_INVULNERABLE, this.invulnerable)
    }

    /**
     * The name that English name of the type of this entity.
     */
    open fun getOriginalName(): String {
        return idName ?: idConvertToName()
    }

    /**
     * Similar to [.getName], but if the name is blank or empty it returns the static name instead.
     */
    fun getVisibleName(): String {
        val name: String = getEntityName()
        return if (TextFormat.clean(name).trim { it <= ' ' }.isNotEmpty()) {
            name
        } else {
            getOriginalName()
        }
    }

    /**
     * The current name used by this entity in the name tag, or the static name if the entity don't have nametag.
     */
    open fun getEntityName(): String {
        return if (this.hasCustomName()) {
            this.getNameTag()
        } else {
            this.getOriginalName()
        }
    }

    /**
     * 将这个实体在客户端生成，让该玩家可以看到它
     *
     *
     * Spawn this entity on the client side so that the player can see it
     *
     * @param player the player
     */
    open fun spawnTo(player: Player) {
        if (!hasSpawned.containsKey(player.loaderId) && this.chunk != null && player.usedChunks.contains(
                Level.chunkHash(
                    chunk!!.x, chunk!!.z
                )
            )
        ) {
            hasSpawned[player.loaderId] = player
            player.sendPacket(createAddEntityPacket())
        }

        if (this.riding != null) {
            riding!!.spawnTo(player)

            val packet = org.chorus_oss.protocol.packets.SetActorLinkPacket(
                actorLink = ActorLink(
                    riddenActorUniqueID = riding!!.getUniqueID(),
                    riderActorUniqueID = this.getUniqueID(),
                    type = ActorLink.Companion.Type.Rider,
                    immediate = true,
                    riderInitiated = false,
                    vehicleAngularVelocity = 0f,
                )
            )
            player.sendPacket(packet)
        }
    }

    protected open fun createAddEntityPacket(): Packet {
        return org.chorus_oss.protocol.packets.AddActorPacket(
            actorUniqueID = this.uniqueId,
            actorRuntimeID = this.runtimeId.toULong(),
            actorType = this.getEntityIdentifier(),
            position = org.chorus_oss.protocol.types.Vector3f(this.position),
            velocity = org.chorus_oss.protocol.types.Vector3f(this.motion),
            rotation = org.chorus_oss.protocol.types.Vector2f(this.rotation),
            headYaw = when (this) {
                is EntityMob -> this.headYaw.toFloat()
                else -> this.rotation.yaw.toFloat()
            },
            bodyYaw = this.rotation.yaw.toFloat(),
            attributes = this.attributes.values.map(AttributeValue::invoke),
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

    val viewers: Map<Int, Player>
        get() = this.hasSpawned

    fun sendPotionEffects(player: Player) {
        for (effect: Effect in effects.values) {
            val packet = MobEffectPacket()
            packet.eid = this.getRuntimeID()
            packet.effectId = effect.getId()
            packet.amplifier = effect.getAmplifier()
            packet.particles = effect.isVisible()
            packet.duration = effect.getDuration()
            packet.eventId = MobEffectPacket.EVENT_ADD.toInt()
            player.dataPacket(packet)
        }
    }

    @JvmOverloads
    fun sendData(player: Player, data: EntityDataMap? = null) {
        val pk = SetActorDataPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            actorDataMap = ActorDataMap(data ?: this.entityDataMap),
            actorProperties = ActorProperties(this.propertySyncData()),
            tick = 0uL
        )
        player.sendPacket(pk)
    }

    @JvmOverloads
    fun sendData(players: Array<Player>, data: EntityDataMap? = null) {
        val pk = SetActorDataPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            actorDataMap = ActorDataMap(data ?: this.entityDataMap),
            actorProperties = ActorProperties(this.propertySyncData()),
            tick = 0uL
        )
        for (player: Player in players) {
            if (player === this) {
                continue
            }
            player.sendPacket(pk)
        }
        if (this is Player) {
            player.sendPacket(pk)
        }
    }

    open fun despawnFrom(player: Player) {
        if (hasSpawned.containsKey(player.loaderId)) {
            val pk = org.chorus_oss.protocol.packets.RemoveActorPacket(
                actorUniqueID = this.getUniqueID()
            )
            player.sendPacket(pk)
            hasSpawned.remove(player.loaderId)
        }
    }

    /**
     * 当一个实体被攻击时(即接受一个实体伤害事件 这个事件可以是由其他实体攻击导致，也可能是自然伤害)调用.
     *
     *
     * Called when an entity is attacked (i.e. receives an entity damage event. This event can be caused by an attack by another entity, or it can be a natural damage).
     *
     * @param source 记录伤害源的事件<br></br>Record the event of the source of the attack
     * @return 是否攻击成功<br></br>Whether the attack was successful
     */
    open fun attack(source: EntityDamageEvent): Boolean {
        //火焰保护附魔实现
        if (hasEffect(EffectType.FIRE_RESISTANCE)
            && (source.cause == DamageCause.FIRE || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.LAVA)
        ) {
            return false
        }

        //水生生物免疫溺水
        if (this is EntitySwimmable && !this.canDrown() && source.cause == DamageCause.DROWNING) return false

        //飞行生物免疫摔伤
        if (this is EntityFlyable && !this.hasFallingDamage() && source.cause == DamageCause.FALL) return false

        //事件回调函数
        Server.instance.pluginManager.callEvent(source)
        if (source.cancelled) {
            return false
        }

        // Make fire aspect to set the target in fire before dealing any damage so the target is in fire on death even if killed by the first hit
        if (source is EntityDamageByEntityEvent) {
            val enchantments: Array<Enchantment>? = source.weaponEnchantments
            if (enchantments != null) {
                for (enchantment: Enchantment in enchantments) {
                    enchantment.doAttack(source)
                }
            }
        }

        //吸收伤害实现
        if (this.absorption > 0) {  // Damage Absorption
            this.setAbsorption(
                max(
                    0.0,
                    (this.getAbsorption() + source.getDamage(DamageModifier.ABSORPTION)).toDouble()
                ).toFloat()
            )
        }

        //修改最后一次伤害
        setLastDamageCause(source)

        //计算血量
        val newHealth: Float = health - source.finalDamage

        //only player
        if (newHealth < 1 && this is Player) {
            if (source.cause != DamageCause.VOID && source.cause != DamageCause.SUICIDE) {
                var totem = false
                var isOffhand = false
                if (player.offhandInventory.getItem(0) is ItemTotemOfUndying) {
                    totem = true
                    isOffhand = true
                } else if (player.inventory.itemInHand is ItemTotemOfUndying) {
                    totem = true
                }
                //复活图腾实现
                if (totem) {
                    level!!.addLevelEvent(this.position, LevelEventPacket.EVENT_SOUND_TOTEM_USED)
                    level!!.addParticleEffect(this.position, ParticleEffect.TOTEM)

                    this.extinguish()
                    this.removeAllEffects()
                    this.setHealthSafe(1f)

                    this.addEffect(
                        Effect.get(EffectType.REGENERATION).setDuration(800).setAmplifier(1)
                    )
                    this.addEffect(Effect.get(EffectType.FIRE_RESISTANCE).setDuration(800))
                    this.addEffect(
                        Effect.get(EffectType.ABSORPTION).setDuration(100).setAmplifier(1)
                    )

                    val pk = EntityEventPacket()
                    pk.eid = this.getRuntimeID()
                    pk.event = EntityEventPacket.CONSUME_TOTEM
                    player.dataPacket(pk)

                    if (isOffhand) {
                        player.offhandInventory.clear(0, true)
                    } else {
                        player.inventory.clear(player.inventory.heldItemIndex, true)
                    }

                    source.cancelled = true
                    return false
                }
            }
        }

        val attacker: Entity? = if (source is EntityDamageByEntityEvent) source.damager else null

        setHealthSafe(newHealth)

        if (this !is EntityArmorStand) {
            level!!.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    attacker,
                    this.position, VibrationType.ENTITY_DAMAGE
                )
            )
        }

        return true
    }

    fun attack(damage: Float): Boolean {
        return this.attack(EntityDamageEvent(this, DamageCause.CUSTOM, damage))
    }

    open fun getNetworkID(): Int {
        return Registries.ENTITY.getEntityNetworkId(getEntityIdentifier())
    }

    fun heal(source: EntityRegainHealthEvent) {
        Server.instance.pluginManager.callEvent(source)
        if (source.cancelled) {
            return
        }
        this.setHealthSafe(this.health + source.amount)
    }

    fun heal(amount: Float) {
        this.heal(EntityRegainHealthEvent(this, amount, EntityRegainHealthEvent.CAUSE_REGEN))
    }

    open fun setHealthSafe(health: Float) {
        if (this.health == health) {
            return
        }

        if (health < 1) {
            if (this.isAlive()) {
                this.kill()
            }
        } else if (health <= this.getMaxHealth() || health < this.health) {
            this.health = health
        } else {
            this.health = getMaxHealth().toFloat()
        }

        setDataProperty(EntityDataTypes.STRUCTURAL_INTEGRITY, this.health.toInt())
    }

    fun isAlive(): Boolean {
        return this.health > 0
    }

    fun isClosed(): Boolean {
        return closed
    }

    fun getLastDamageCause(): EntityDamageEvent? {
        return lastDamageCause
    }

    fun setLastDamageCause(type: EntityDamageEvent?) {
        this.lastDamageCause = type
    }

    fun getMaxHealth(): Int {
        return maxHealth
    }

    open fun setMaxHealth(maxHealth: Int) {
        this.maxHealth = maxHealth
    }

    open fun canCollideWith(entity: Entity): Boolean {
        return !this.justCreated && this !== entity && !this.noClip
    }

    /**
     * Whether the entity is persisted to disk
     *
     * @return the boolean
     */
    fun canBeSavedWithChunk(): Boolean {
        return saveWithChunk
    }


    /**
     * Set this entity is persisted to disk
     *
     * @param saveWithChunk value
     */
    fun setCanBeSavedWithChunk(saveWithChunk: Boolean) {
        this.saveWithChunk = saveWithChunk
    }

    protected fun checkObstruction(x: Double, y: Double, z: Double): Boolean {
        if (level!!.fastCollisionCubes(this, this.getBoundingBox(), false).isEmpty() || this.noClip) {
            return false
        }

        val i: Int = floor(x).toInt()
        val j: Int = floor(y).toInt()
        val k: Int = floor(z).toInt()

        val diffX: Double = x - i
        val diffY: Double = y - j
        val diffZ: Double = z - k

        if (level!!.getBlock(i, j, k).isTransparent == false) {
            val flag: Boolean = level!!.getBlock(i - 1, j, k).isTransparent
            val flag1: Boolean = level!!.getBlock(i + 1, j, k).isTransparent
            val flag2: Boolean = level!!.getBlock(i, j - 1, k).isTransparent
            val flag3: Boolean = level!!.getBlock(i, j + 1, k).isTransparent
            val flag4: Boolean = level!!.getBlock(i, j, k - 1).isTransparent
            val flag5: Boolean = level!!.getBlock(i, j, k + 1).isTransparent

            var direction: Int = -1
            var limit = 9999.0

            if (flag) {
                limit = diffX
                direction = 0
            }

            if (flag1 && 1 - diffX < limit) {
                limit = 1 - diffX
                direction = 1
            }

            if (flag2 && diffY < limit) {
                limit = diffY
                direction = 2
            }

            if (flag3 && 1 - diffY < limit) {
                limit = 1 - diffY
                direction = 3
            }

            if (flag4 && diffZ < limit) {
                limit = diffZ
                direction = 4
            }

            if (flag5 && 1 - diffZ < limit) {
                direction = 5
            }

            val force: Double = ThreadLocalRandom.current().nextDouble() * 0.2 + 0.1

            if (direction == 0) {
                motion.x = -force

                return true
            }

            if (direction == 1) {
                motion.x = force

                return true
            }

            if (direction == 2) {
                motion.y = -force

                return true
            }

            if (direction == 3) {
                motion.y = force

                return true
            }

            if (direction == 4) {
                motion.z = -force

                return true
            }

            if (direction == 5) {
                motion.z = force

                return true
            }
        }

        return false
    }

    open fun entityBaseTick(): Boolean {
        return this.entityBaseTick(1)
    }

    open fun entityBaseTick(tickDiff: Int): Boolean {
        if (!this.isAlive()) {
            if (this is EntityCreature) {
                this.deadTicks += tickDiff
                if (this.deadTicks >= 15) {
                    //apply death smoke cloud only if it is a creature
                    val aabb: AxisAlignedBB = this.getBoundingBox()
                    var x: Double = aabb.minX
                    while (x <= aabb.maxX) {
                        var z: Double = aabb.minZ
                        while (z <= aabb.maxZ) {
                            var y: Double = aabb.minY
                            while (y <= aabb.maxY) {
                                level!!.addParticle(ExplodeParticle(Vector3(x, y, z)))
                                y += 0.5
                            }
                            z += 0.5
                        }
                        x += 0.5
                    }
                    this.despawnFromAll()
                    if (!this.isPlayer) {
                        this.close()
                    }
                }
                return this.deadTicks < 15
            } else {
                this.despawnFromAll()
                if (!this.isPlayer) {
                    this.close()
                }
            }
        }
        if (!this.isPlayer) {
            this.blocksAround = null
            this.collisionBlocks = null
        }
        this.justCreated = false

        if (riding != null && !riding!!.isAlive() && riding is EntityRideable) {
            riding!!.dismountEntity(this)
        }
        updatePassengers()

        if (effects.isNotEmpty()) {
            for (effect: Effect in effects.values) {
                if (effect.canTick()) {
                    effect.apply(this, 1.0)
                }
                effect.setDuration(effect.getDuration() - tickDiff)

                if (effect.getDuration() <= 0) {
                    this.removeEffect(effect.getType())
                }
            }
        }

        var hasUpdate = false

        this.checkBlockCollision()

        if (position.y < (level!!.minHeight - 18) && this.isAlive()) {
            if (this is Player) {
                if (!player.isCreative) this.attack(EntityDamageEvent(this, DamageCause.VOID, 10f))
            } else {
                this.attack(EntityDamageEvent(this, DamageCause.VOID, 10f))
                hasUpdate = true
            }
        }

        if (this.fireTicks > 0) {
            if (this.fireProof) {
                this.fireTicks -= 4 * tickDiff
                if (this.fireTicks < 0) {
                    this.fireTicks = 0
                }
            } else {
                if (!this.hasEffect(EffectType.FIRE_RESISTANCE) && ((this.fireTicks % 20) == 0 || tickDiff > 20)) {
                    this.attack(EntityDamageEvent(this, DamageCause.FIRE_TICK, 1f))
                }
                this.fireTicks -= tickDiff
            }
            if (this.fireTicks <= 0) {
                this.extinguish()
            } else if (!this.fireProof && (this !is Player || !player.isSpectator)) {
                this.setDataFlag(EntityFlag.ON_FIRE)
                hasUpdate = true
            }
        }

        if (this.noDamageTicks > 0) {
            this.noDamageTicks -= tickDiff
            if (this.noDamageTicks < 0) {
                this.noDamageTicks = 0
            }
        }

        if (this.inPortalTicks == 80) { //handle portal teleport
            val ev = EntityPortalEnterEvent(this, PortalType.NETHER)
            Server.instance.pluginManager.callEvent(ev) //call event

            if (!ev.cancelled && (level!!.dimension == Level.DIMENSION_OVERWORLD || level!!.dimension == Level.DIMENSION_NETHER)) {
                val newPos: Locator? = PortalHelper.convertPosBetweenNetherAndOverworld(
                    Locator(
                        position.x, position.y, position.z,
                        level!!
                    )
                )
                if (newPos != null) {
                    val nearestPortal: Locator? = PortalHelper.getNearestValidPortal(newPos)
                    if (nearestPortal != null) {
                        teleport(nearestPortal.add(0.5, 0.0, 0.5), TeleportCause.NETHER_PORTAL)
                    } else {
                        val finalPos: Locator = newPos.add(1.5, 1.0, 1.5)
                        if (teleport(finalPos, TeleportCause.NETHER_PORTAL)) {
                            level!!.scheduler.scheduleDelayedTask(object : Task() {
                                override fun onRun(currentTick: Int) {
                                    // dirty hack to make sure chunks are loaded and generated before spawning
                                    // player
                                    inPortalTicks = 81
                                    teleport(finalPos, TeleportCause.NETHER_PORTAL)
                                    PortalHelper.spawnPortal(newPos)
                                }
                            }, 5)
                        }
                    }
                }
            }
        }
        this.age += tickDiff
        this.ticksLived += tickDiff

        return hasUpdate
    }

    @JvmOverloads
    fun hasPosChanged(threshold: Double = 0.0001): Boolean {
        return position.subtract(this.prevPosition).lengthSquared() > threshold
    }

    fun hasRotationChanged(): Boolean {
        return this.hasRotationChanged(1.0)
    }

    open fun hasRotationChanged(threshold: Double): Boolean {
        return rotation.subtract(this.prevRotation).lengthSquared() > threshold
    }

    @JvmOverloads
    fun hasMotionChanged(threshold: Double = 0.0001): Boolean {
        return motion.subtract(this.prevMotion).lengthSquared() > threshold
    }

    open fun updateMovement() {
        val posChanged: Boolean = this.hasPosChanged()
        val rotationChanged: Boolean = this.hasRotationChanged()

        if (posChanged || rotationChanged) { //0.2 ** 2, 1.5 ** 2
            if (posChanged) {
                if (this.isOnGround()) {
                    level!!.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            if (this is EntityProjectile) this.shootingEntity else this,
                            position.clone(), VibrationType.STEP
                        )
                    )
                } else if (this.isTouchingWater()) {
                    level!!.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            if (this is EntityProjectile) this.shootingEntity else this,
                            position.clone(), VibrationType.SWIM
                        )
                    )
                }
            }

            this.moveDelta()

            prevPosition.x = position.x
            prevPosition.y = position.y
            prevPosition.z = position.z

            prevRotation.pitch = rotation.pitch
            prevRotation.yaw = rotation.yaw

            this.positionChanged = true
        } else {
            this.positionChanged = false
        }

        if (this.hasMotionChanged(0.025) || (this.hasMotionChanged(0.0001) && getMotion().lengthSquared() <= 0.0001)) { //0.05 ** 2
            prevMotion.x = motion.x
            prevMotion.y = motion.y
            prevMotion.z = motion.z

            this.addMotion(motion.x, motion.y, motion.z)
        }
    }

    /**
     * 增加运动 (仅发送数据包，如果需要请使用[.setMotion])
     *
     *
     * Add motion (just sending packet will not make the entity actually move, use [.setMotion] if needed)
     *
     */
    open fun moveDelta() {
        val pk = MoveEntityDeltaPacket()
        pk.runtimeEntityId = this.getRuntimeID()
        if (prevPosition.x != position.x) {
            pk.x = position.x.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_X.toInt()).toShort()
        }
        if (prevPosition.y != position.y) {
            pk.y = position.y.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_Y.toInt()).toShort()
        }
        if (prevPosition.z != position.z) {
            pk.z = position.z.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_Z.toInt()).toShort()
        }
        if (prevRotation.pitch != rotation.pitch) {
            pk.pitch = rotation.pitch.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_PITCH.toInt()).toShort()
        }
        if (prevRotation.yaw != rotation.yaw) {
            pk.yaw = rotation.yaw.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_YAW.toInt()).toShort()
        }
        if (this.onGround) {
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_ON_GROUND.toInt()).toShort()
        }
        Server.broadcastPacket(viewers.values, pk)
    }

    /*
     * 请注意此方法仅向客户端发motion包，并不会真正的将motion数值加到实体的motion(x|y|z)上<p/>
     * 如果你想在实体的motion基础上增加，请直接将要添加的motion数值加到实体的motion(x|y|z)上，像这样：<p/>
     * entity.motionX += vector3.x;<p/>
     * entity.motionY += vector3.y;<p/>
     * entity.motionZ += vector3.z;<p/>
     * 对于玩家实体，你不应该使用此方法！
     */
    fun addMotion(motionX: Double, motionY: Double, motionZ: Double) {
        val pk = SetActorMotionPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            motion = org.chorus_oss.protocol.types.Vector3f(
                motionX.toFloat(),
                motionY.toFloat(),
                motionZ.toFloat()
            ),
            tick = 0uL,
        )
        Server.broadcastPacket(hasSpawned.values, pk)
    }


    protected fun broadcastMovement(tp: Boolean) {
        val pk = MoveEntityAbsolutePacket()
        pk.eid = this.getRuntimeID()
        pk.x = position.x
        pk.y = position.y + this.getBaseOffset()
        pk.z = position.z
        pk.headYaw = rotation.yaw
        pk.pitch = rotation.pitch
        pk.yaw = rotation.yaw
        pk.teleport = tp
        pk.onGround = this.onGround
        Server.broadcastPacket(hasSpawned.values, pk)
    }

    fun getDirectionVector(): Vector3 {
        val pitch: Double = ((rotation.pitch + 90) * Math.PI) / 180
        val yaw: Double = ((rotation.yaw + 90) * Math.PI) / 180
        val x: Double = sin(pitch) * cos(yaw)
        val z: Double = sin(pitch) * sin(yaw)
        val y: Double = cos(pitch)
        return Vector3(x, y, z).normalize()
    }

    fun getDirectionPlane(): Vector2 {
        return (Vector2(
            (-cos(Math.toRadians(rotation.yaw) - Math.PI / 2)).toFloat().toDouble(), (-sin(
                Math.toRadians(
                    rotation.yaw
                ) - Math.PI / 2
            )).toFloat().toDouble()
        )).normalize()
    }

    fun getHorizontalFacing(): BlockFace {
        return BlockFace.fromHorizontalIndex(floor((rotation.yaw * 4.0f / 360.0f) + 0.5).toInt() and 3)
    }

    open fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        val tickDiff: Int = currentTick - this.lastUpdate
        if (tickDiff <= 0) {
            return false
        }
        this.lastUpdate = currentTick

        val hasUpdate: Boolean = this.entityBaseTick(tickDiff)

        if (!this.isImmobile()) {
            this.updateMovement()
        }

        return hasUpdate
    }

    open fun mountEntity(entity: Entity): Boolean {
        return mountEntity(entity, ActorLink.Companion.Type.Rider)
    }

    /**
     * Mount an Entity from a/into vehicle
     *
     * @param entity The target Entity
     * @return `true` if the mounting successful
     */
    open fun mountEntity(entity: Entity, mode: ActorLink.Companion.Type): Boolean {
        requireNonNull(entity, "The target of the mounting entity can't be null")

        if (isPassenger(entity) || entity.riding != null && !entity.riding!!.dismountEntity(entity, false)) {
            return false
        }

        // Entity entering a vehicle
        val ev = EntityVehicleEnterEvent(entity, this)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }

        broadcastLinkPacket(entity, mode)

        // Add variables to entity
        entity.riding = this
        entity.setDataFlag(EntityFlag.RIDING)
        passengers.add(entity)

        entity.setSeatPosition(getMountedOffset(entity))
        updatePassengerPosition(entity)
        return true
    }

    open fun dismountEntity(entity: Entity): Boolean {
        return this.dismountEntity(entity, true)
    }

    open fun dismountEntity(entity: Entity, sendLinks: Boolean): Boolean {
        // Run the events
        val ev = EntityVehicleExitEvent(entity, this)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            val seatIndex: Int = passengers.indexOf(entity)
            if (seatIndex == 0) {
                this.broadcastLinkPacket(entity, ActorLink.Companion.Type.Rider)
            } else if (seatIndex != -1) {
                this.broadcastLinkPacket(entity, ActorLink.Companion.Type.Passenger)
            }
            return false
        }

        if (sendLinks) {
            broadcastLinkPacket(entity, ActorLink.Companion.Type.Remove)
        }

        // refresh the entity
        entity.riding = null
        entity.setDataFlag(EntityFlag.RIDING, false)
        passengers.remove(entity)

        entity.setSeatPosition(Vector3f())
        updatePassengerPosition(entity)

        if (entity is Player) {
            entity.resetFallDistance()
        }
        return true
    }

    protected fun broadcastLinkPacket(rider: Entity, type: ActorLink.Companion.Type) {
        val pk = org.chorus_oss.protocol.packets.SetActorLinkPacket(
            actorLink = ActorLink(
                riddenActorUniqueID = this.getUniqueID(),
                riderActorUniqueID = rider.getUniqueID(),
                type = type,
                immediate = false,
                riderInitiated = type != ActorLink.Companion.Type.Remove,
                vehicleAngularVelocity = 0f,
            )
        )
        Server.broadcastPacket(hasSpawned.values, pk)
    }

    open fun updatePassengers() {
        if (passengers.isEmpty()) {
            return
        }

        for (passenger: Entity in ArrayList(this.passengers)) {
            if (!passenger.isAlive()) {
                dismountEntity(passenger)
                continue
            }

            updatePassengerPosition(passenger)
        }
    }

    protected open fun updatePassengerPosition(passenger: Entity) {
        passenger.setPosition(position.add(passenger.getSeatPosition().asVector3()))
    }

    fun getSeatPosition(): Vector3f {
        return this.getDataProperty(EntityDataTypes.SEAT_OFFSET)
    }

    fun setSeatPosition(pos: Vector3f) {
        this.setDataProperty(EntityDataTypes.SEAT_OFFSET, pos)
    }

    open fun getMountedOffset(entity: Entity?): Vector3f {
        return Vector3f(0f, getHeight() * 0.75f)
    }

    fun scheduleUpdate() {
        level!!.updateEntities.put(this.getRuntimeID(), this)
    }

    fun isOnFire(): Boolean {
        return this.fireTicks > 0
    }

    open fun setOnFire(seconds: Int) {
        val ticks: Int = seconds * 20
        if (ticks > this.fireTicks) {
            this.fireTicks = ticks
        }
    }

    fun getAbsorption(): Float {
        return absorption
    }

    open fun setAbsorption(absorption: Float) {
        if (absorption != this.absorption) {
            this.absorption = absorption
        }
    }

    open fun syncAttribute(attribute: Attribute) {
        val pk = UpdateAttributesPacket()
        pk.entries = arrayOf(attribute)
        pk.entityId = this.getRuntimeID()
        Server.broadcastPacket(viewers.values, pk)
    }

    open fun syncAttributes() {
        val pk = UpdateAttributesPacket()
        pk.entries =
            attributes.values.stream().filter { obj: Attribute -> obj.isSyncable() }.toList().toTypedArray()
        pk.entityId = this.getRuntimeID()
        Server.broadcastPacket(viewers.values, pk)
    }


    fun canBePushed(): Boolean {
        return true
    }

    open fun getDirection(): BlockFace {
        var rotation: Double = rotation.yaw % 360
        if (rotation < 0) {
            rotation += 360.0
        }
        return when {
            (0 <= rotation && rotation < 45) || (315 <= rotation && rotation < 360) -> BlockFace.SOUTH
            45 <= rotation && rotation < 135 -> BlockFace.WEST
            135 <= rotation && rotation < 225 -> BlockFace.NORTH
            225 <= rotation && rotation < 315 -> BlockFace.EAST
            else -> BlockFace.SOUTH
        }
    }

    fun extinguish() {
        this.fireTicks = 0
        this.setDataFlag(EntityFlag.ON_FIRE, false)
    }

    open fun resetFallDistance() {
        this.highestPosition = level!!.minHeight.toDouble()
    }

    protected open fun updateFallState(onGround: Boolean) {
        if (onGround) {
            fallDistance = (this.highestPosition - position.y).toFloat()

            if (fallDistance > 0) {
                val pos = Locator(
                    position.x,
                    position.y, position.z,
                    level!!
                )
                // check if we fell into at least 1 block of water
                val lb: Block = pos.levelBlock
                val lb2: Block = pos.getLevelBlockAtLayer(1)
                if (this is EntityLiving && this.riding == null && !(lb is BlockFlowingWater ||
                            lb is BlockFence ||
                            (lb2 is BlockFlowingWater && lb2.maxY == 1.0))
                ) {
                    this.fall(fallDistance)
                }
                this.resetFallDistance()
            }
        }
    }

    fun getBoundingBox(): AxisAlignedBB {
        return this.boundingBox
    }

    open fun fall(fallDistance: Float) { // TODO: check why @param fallDistance always less than the real distance
        var fallDistance1: Float = fallDistance
        if (this.hasEffect(EffectType.SLOW_FALLING)) {
            return
        }

        val floorLocation: Vector3 = position.floor()
        val down: Block = level!!.getBlock(floorLocation.down())

        val event = EntityFallEvent(this, down, fallDistance1)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return
        }
        fallDistance1 = event.fallDistance

        if ((!this.isPlayer || level!!.gameRules
                .getBoolean(GameRule.FALL_DAMAGE)) && down.useDefaultFallDamage()
        ) {
            val jumpBoost: Int = if (this.hasEffect(EffectType.JUMP_BOOST)) getEffect(
                EffectType.JUMP_BOOST
            )!!.getLevel() else 0
            val damage: Float = fallDistance1 - 3.255f - jumpBoost

            if (damage > 0) {
                if (!this.isSneaking()) {
                    if (this !is EntityItem ||
                        !ItemTags.getTagSet(item.identifier.toString()).contains(ItemTags.WOOL)
                    ) {
                        level!!.vibrationManager.callVibrationEvent(
                            VibrationEvent(
                                this,
                                position.clone(), VibrationType.HIT_GROUND
                            )
                        )
                    }
                }
                this.attack(EntityDamageEvent(this, DamageCause.FALL, damage))
            }
        }

        down.onEntityFallOn(this, fallDistance1)

        if (fallDistance1 > 0.75) { // TODO: moving these into their own classes (method "onEntityFallOn()")
            if (BlockID.FARMLAND == down.id) {
                if (onPhysicalInteraction(down, false)) {
                    return
                }
                if (this is EntityFlyable) return
                val farmEvent = FarmLandDecayEvent(this, down)
                Server.instance.pluginManager.callEvent(farmEvent)
                if (farmEvent.cancelled) return
                level!!.setBlock(down.position, Block.get(BlockID.DIRT), direct = false, update = true)
                return
            }

            val floor: Block = level!!.getBlock(floorLocation)

            if (floor is BlockTurtleEgg) {
                if (onPhysicalInteraction(floor, ThreadLocalRandom.current().nextInt(10) >= 3)) {
                    return
                }
                level!!.useBreakOn(this.position, null, null, true)
            }
        }
    }

    protected fun onPhysicalInteraction(block: Block, cancelled: Boolean): Boolean {

        val ev: Event = if (this is Player) {
            PlayerInteractEvent(this, null, block.position, null, PlayerInteractEvent.Action.PHYSICAL)
        } else {
            EntityInteractEvent(this, block)
        }

        ev.cancelled = cancelled

        Server.instance.pluginManager.callEvent(ev)
        return ev.cancelled
    }

    open fun applyEntityCollision(entity: Entity) {
        if (entity.riding !== this && !entity.passengers.contains(this)) {
            var dx: Double = entity.position.x - position.x
            var dy: Double = entity.position.z - position.z
            var dz: Double = max(abs(dx), abs(dy))

            if (dz >= 0.009999999776482582) {
                dz = sqrt(dz.toFloat()).toDouble()
                dx /= dz
                dy /= dz
                var d3: Double = 1.0 / dz

                if (d3 > 1.0) {
                    d3 = 1.0
                }

                dx *= d3
                dy *= d3
                dx *= 0.05000000074505806
                dy *= 0.05000000074505806
                dx *= 1f + entityCollisionReduction

                if (this.riding == null) {
                    motion.x -= dx
                    motion.y -= dy
                }
            }
        }
    }

    open fun onStruckByLightning(entity: Entity) {
        if (this.attack(EntityDamageByEntityEvent(entity, this, DamageCause.LIGHTNING, 5f))) {
            if (this.fireTicks < 8 * 20) {
                this.setOnFire(8)
            }
        }
    }


    open fun onPushByPiston(piston: BlockEntityPistonArm?) {
    }

    open fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        return onInteract(player, item)
    }

    fun onInteract(player: Player?, item: Item?): Boolean {
        return false
    }

    protected open fun switchLevel(targetLevel: Level): Boolean {
        if (this.closed) {
            return false
        }

        val ev = EntityLevelChangeEvent(this, this.level!!, targetLevel)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }

        level!!.removeEntity(this)
        if (this.chunk != null) {
            chunk!!.removeEntity(this)
        }
        this.despawnFromAll()

        this.level = targetLevel
        level!!.addEntity(this)
        this.chunk = null
        this.lastUpdate = level!!.tick
        this.blocksAround = null
        this.collisionBlocks = null
        return true
    }

    val transform: Transform
        get() = Transform(
            position.x,
            position.y,
            position.z, rotation.yaw, rotation.pitch, rotation.yaw,
            level!!
        )

    val locator: Locator
        get() = Locator(
            position.x, position.y, position.z,
            level!!
        )

    fun isTouchingWater(): Boolean {
        return hasWaterAt(0f) || hasWaterAt(this.getEyeHeight())
    }

    fun isInsideOfWater(): Boolean {
        return hasWaterAt(this.getEyeHeight())
    }


    fun isUnderBlock(): Boolean {
        val x: Int = position.floorX
        val y: Int = position.floorY
        val z: Int = position.floorZ
        for (i in y + 1..level!!.maxHeight) {
            if (!level!!.getBlock(x, i, z).isAir) {
                return true
            }
        }
        return false
    }


    fun hasWaterAt(height: Float): Boolean {
        return hasWaterAt(height, false)
    }


    protected fun hasWaterAt(height: Float, tickCached: Boolean): Boolean {
        val y: Double = position.y + height
        val block: Block = if (tickCached) level!!.getTickCachedBlock(
            position.floor()
        ) else level!!.getBlock(position.floor())

        var layer1 = false
        val block1: Block =
            if (tickCached) block.getTickCachedLevelBlockAtLayer(1) else block.getLevelBlockAtLayer(1)
        if (block !is BlockBubbleColumn && (block is BlockFlowingWater
                    || (block1 is BlockFlowingWater).also { layer1 = it }
                    )
        ) {
            val water: BlockFlowingWater = (if (layer1) block1 else block) as BlockFlowingWater
            val f: Double = (block.position.y + 1) - (water.fluidHeightPercent - 0.1111111)
            return y < f
        }

        return false
    }

    fun isInsideOfSolid(): Boolean {
        val y: Double = position.y + this.getEyeHeight()
        val block: Block = level!!.getBlock(
            position.clone().setY(y).floor()
        )

        val bb: AxisAlignedBB? = block.boundingBox

        return bb != null && block.isSolid && !block.isTransparent && bb.intersectsWith(this.getBoundingBox())
    }

    fun isInsideOfFire(): Boolean {
        for (block: Block? in getCollisionBlocks()!!) {
            if (block is BlockFire) {
                return true
            }
        }

        return false
    }


    fun <T : Block?> collideWithBlock(classType: Class<T>): Boolean {
        for (block: Block? in getCollisionBlocks()!!) {
            if (classType.isInstance(block)) {
                return true
            }
        }
        return false
    }


    fun isInsideOfLava(): Boolean {
        for (block: Block? in getCollisionBlocks()!!) {
            if (block is BlockFlowingLava) {
                return true
            }
        }

        return false
    }

    fun isOnLadder(): Boolean {
        val b: Block = transform.levelBlock

        return BlockID.LADDER == b.id
    }

    /**
     * Player do not use
     */
    open fun move(dx: Double, dy: Double, dz: Double): Boolean {
        var dx1: Double = dx
        var dy1: Double = dy
        var dz1: Double = dz
        if (dx1 == 0.0 && dz1 == 0.0 && dy1 == 0.0) {
            val value: Locator = this.transform
            value.position.setComponents(position.down())
            this.onGround = !value.tickCachedLevelBlock.canPassThrough()
            return true
        }


        this.ySize *= 0.4f

        val movX: Double = dx1
        val movY: Double = dy1
        val movZ: Double = dz1

        val axisAlignedBB: AxisAlignedBB = boundingBox.clone()

        var list: List<AxisAlignedBB> = if (this.noClip) AxisAlignedBB.EMPTY_LIST else level!!.fastCollisionCubes(
            this,
            boundingBox.addCoord(dx1, dy1, dz1), false
        )

        for (bb: AxisAlignedBB in list) {
            dy1 = bb.calculateYOffset(this.boundingBox, dy1)
        }

        boundingBox.offset(0.0, dy1, 0.0)

        val fallingFlag: Boolean = (this.onGround || (dy1 != movY && movY < 0))

        for (bb: AxisAlignedBB in list) {
            dx1 = bb.calculateXOffset(this.boundingBox, dx1)
        }

        boundingBox.offset(dx1, 0.0, 0.0)

        for (bb: AxisAlignedBB in list) {
            dz1 = bb.calculateZOffset(this.boundingBox, dz1)
        }

        boundingBox.offset(0.0, 0.0, dz1)

        if (this.getStepHeight() > 0 && fallingFlag && this.ySize < 0.05 && (movX != dx1 || movZ != dz1)) {
            val cx: Double = dx1
            val cy: Double = dy1
            val cz: Double = dz1
            dx1 = movX
            dy1 = this.getStepHeight()
            dz1 = movZ

            val axisAlignedBB1: AxisAlignedBB = boundingBox.clone()

            boundingBox.setBB(axisAlignedBB)

            list = level!!.fastCollisionCubes(
                this,
                boundingBox.addCoord(dx1, dy1, dz1), false
            )

            for (bb: AxisAlignedBB in list) {
                dy1 = bb.calculateYOffset(this.boundingBox, dy1)
            }

            boundingBox.offset(0.0, dy1, 0.0)

            for (bb: AxisAlignedBB in list) {
                dx1 = bb.calculateXOffset(this.boundingBox, dx1)
            }

            boundingBox.offset(dx1, 0.0, 0.0)

            for (bb: AxisAlignedBB in list) {
                dz1 = bb.calculateZOffset(this.boundingBox, dz1)
            }

            boundingBox.offset(0.0, 0.0, dz1)

            boundingBox.offset(0.0, 0.0, dz1)

            if ((cx * cx + cz * cz) >= (dx1 * dx1 + dz1 * dz1)) {
                dx1 = cx
                dy1 = cy
                dz1 = cz
                boundingBox.setBB(axisAlignedBB1)
            } else {
                this.ySize += 0.5f
            }
        }

        position.x = (boundingBox.minX + boundingBox.maxX) / 2
        position.y = boundingBox.minY - this.ySize
        position.z = (boundingBox.minZ + boundingBox.maxZ) / 2

        this.checkChunks()

        this.checkGroundState(movX, movY, movZ, dx1, dy1, dz1)
        this.updateFallState(this.onGround)

        if (movX != dx1) {
            motion.x = 0.0
        }

        if (movY != dy1) {
            motion.y = 0.0
        }

        if (movZ != dz1) {
            motion.z = 0.0
        }

        // TODO: vehicle collision events (first we need to spawn them!)
        return true
    }

    protected open fun checkGroundState(movX: Double, movY: Double, movZ: Double, dx: Double, dy: Double, dz: Double) {
        if (this.noClip) {
            this.isCollidedVertically = false
            this.isCollidedHorizontally = false
            this.isCollided = false
            this.onGround = false
        } else {
            this.isCollidedVertically = movY != dy
            this.isCollidedHorizontally = (movX != dx || movZ != dz)
            this.isCollided = (this.isCollidedHorizontally || this.isCollidedVertically)
            this.onGround = (movY != dy && movY < 0)
        }
    }

    fun getBlocksAround(): List<Block>? {
        if (this.blocksAround == null) {
            val minX: Int = floor(boundingBox.minX).toInt()
            val minY: Int = floor(boundingBox.minY).toInt()
            val minZ: Int = floor(boundingBox.minZ).toInt()
            val maxX: Int = ceil(boundingBox.maxX).toInt()
            val maxY: Int = ceil(boundingBox.maxY).toInt()
            val maxZ: Int = ceil(boundingBox.maxZ).toInt()

            val blocksAround = ArrayList<Block>()

            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block: Block = level!!.getBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
                        if (block != null) blocksAround.add(block)
                    }
                }
            }
            this.blocksAround = blocksAround
        }

        return this.blocksAround
    }


    fun getTickCachedBlocksAround(): List<Block>? {
        if (this.blocksAround == null) {
            val minX: Int = floor(boundingBox.minX).toInt()
            val minY: Int = floor(boundingBox.minY).toInt()
            val minZ: Int = floor(boundingBox.minZ).toInt()
            val maxX: Int = ceil(boundingBox.maxX).toInt()
            val maxY: Int = ceil(boundingBox.maxY).toInt()
            val maxZ: Int = ceil(boundingBox.maxZ).toInt()

            val blocksAround = ArrayList<Block>()

            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        val block = level!!.getTickCachedBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
                        if (block != null) blocksAround.add(block)
                    }
                }
            }
            this.blocksAround = blocksAround
        }

        return this.blocksAround
    }

    fun getCollisionBlocks(): List<Block>? {
        if (this.collisionBlocks == null) {
            val collisionBlocks = ArrayList<Block>()

            for (b: Block in getBlocksAround()!!) {
                if (b.collidesWithBB(this.getBoundingBox(), true)) {
                    collisionBlocks.add(b)
                }
            }
            this.collisionBlocks = collisionBlocks
        }

        return this.collisionBlocks
    }


    fun getTickCachedCollisionBlocks(): List<Block>? {
        if (this.collisionBlocks == null) {
            val collisionBlocks = ArrayList<Block>()

            for (b: Block in getTickCachedBlocksAround()!!) {
                if (b.collidesWithBB(this.getBoundingBox(), true)) {
                    collisionBlocks.add(b)
                }
            }
            this.collisionBlocks = collisionBlocks
        }

        return this.collisionBlocks
    }

    /**
     * Returns whether this entity can be moved by currents in liquids.
     *
     * @return boolean
     */
    open fun canBeMovedByCurrents(): Boolean {
        return true
    }

    protected open fun checkBlockCollision() {
        if (this.noClip) {
            return
        }

        var needsReCalcCurrent = true
        if (this is EntityPhysical) {
            needsReCalcCurrent = this.needsReCalcMovement
        }

        var vector = Vector3(0.0, 0.0, 0.0)
        var portal = false
        var scaffolding = false
        var endPortal = false
        for (block: Block in getTickCachedCollisionBlocks()!!) {
            when (block.id) {
                BlockID.PORTAL -> portal = true
                BlockID.SCAFFOLDING -> scaffolding = true
                BlockID.END_PORTAL -> endPortal = true
            }

            block.onEntityCollide(this)
            block.getTickCachedLevelBlockAtLayer(1).onEntityCollide(this)
            if (needsReCalcCurrent) block.addVelocityToEntity(this, vector)
        }

        setDataFlagExtend(EntityFlag.IN_SCAFFOLDING, scaffolding)

        if (abs(position.y % 1) > 0.125) {
            val minX: Int = floor(boundingBox.minX).toInt()
            val minZ: Int = floor(boundingBox.minZ).toInt()
            val maxX: Int = ceil(boundingBox.maxX).toInt()
            val maxZ: Int = ceil(boundingBox.maxZ).toInt()
            val y: Int = position.y.toInt()

            outerScaffolding@ for (i in minX..maxX) {
                for (j in minZ..maxZ) {
                    val transform = Transform(
                        i.toDouble(), y.toDouble(), j.toDouble(),
                        level!!
                    )
                    if (BlockID.SCAFFOLDING == transform.getLevelBlock(false).id) {
                        setDataFlagExtend(EntityFlag.OVER_SCAFFOLDING, true)
                        break@outerScaffolding
                    }
                }
            }
        }

        if (endPortal) { //handle endPortal teleport
            if (!inEndPortal) {
                inEndPortal = true
                if (this.getRiding() == null && passengers.isEmpty() && (this !is EntityEnderDragon)) {
                    val ev = EntityPortalEnterEvent(this, PortalType.END)
                    Server.instance.pluginManager.callEvent(ev)

                    if (!ev.cancelled && (level!!.dimension == Level.DIMENSION_OVERWORLD || level!!.dimension == Level.DIMENSION_THE_END)) {
                        val newPos: Locator? = PortalHelper.moveToTheEnd(this.transform)
                        if (newPos != null) {
                            if (newPos.level.dimension == Level.DIMENSION_THE_END) {
                                if (teleport(newPos.add(0.5, 1.0, 0.5), TeleportCause.END_PORTAL)) {
                                    newPos.level.scheduler.scheduleDelayedTask(object : Task() {
                                        override fun onRun(currentTick: Int) {
                                            // dirty hack to make sure chunks are loaded and generated before spawning player
                                            teleport(newPos.add(0.5, 1.0, 0.5), TeleportCause.END_PORTAL)
                                            BlockEndPortal.spawnObsidianPlatform(newPos)
                                        }
                                    }, 5)
                                }
                            } else {
                                if (teleport(newPos, TeleportCause.END_PORTAL)) {
                                    newPos.level.scheduler.scheduleDelayedTask(object : Task() {
                                        override fun onRun(currentTick: Int) {
                                            // dirty hack to make sure chunks are loaded and generated before spawning player
                                            teleport(newPos, TeleportCause.END_PORTAL)
                                        }
                                    }, 5)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            inEndPortal = false
        }

        if (portal) {
            if (this.inPortalTicks <= 80) {
                // 81 means the server won't try to teleport
                inPortalTicks++
            }
        } else {
            this.inPortalTicks = 0
        }

        if (needsReCalcCurrent) if (vector.lengthSquared() > 0) {
            vector = vector.normalize()
            val d = 0.018
            val dx: Double = vector.x * d
            val dy: Double = vector.y * d
            val dz: Double = vector.z * d
            motion.x += dx
            motion.y += dy
            motion.z += dz
            if (this is EntityPhysical) {
                this.previousCurrentMotion.x = dx
                this.previousCurrentMotion.y = dy
                this.previousCurrentMotion.z = dz
            }
        } else {
            if (this is EntityPhysical) {
                this.previousCurrentMotion.x = 0.0
                this.previousCurrentMotion.y = 0.0
                this.previousCurrentMotion.z = 0.0
            }
        }
        else if (this is EntityPhysical) this.addPreviousLiquidMovement()
    }

    fun setPositionAndRotation(pos: Vector3, yaw: Double, pitch: Double): Boolean {
        this.setRotation(yaw, pitch)
        return this.setPosition(pos)
    }

    fun setPositionAndRotation(pos: Locator, yaw: Double, pitch: Double): Boolean {
        this.setRotation(yaw, pitch)
        return this.setPosition(pos)
    }

    fun setPosition(pos: Locator): Boolean {
        if (this.closed) {
            return false
        }

        if (pos.level !== this.level) {
            if (!this.switchLevel(pos.level)) {
                return false
            }
        }

        return setPosition(pos.position)
    }

    fun setPosition(pos: Vector3): Boolean {
        if (this.closed) {
            return false
        }

        position.x = pos.x
        position.y = pos.y
        position.z = pos.z

        this.recalculateBoundingBox(false) // Don't need to send BB height/width to client on position change

        this.checkChunks()

        return true
    }

    fun setRotation(yaw: Double, pitch: Double) {
        rotation.yaw = yaw
        rotation.pitch = pitch
        this.scheduleUpdate()
    }

    /**
     * Whether the entity can activate pressure plates.
     * Used for [org.chorus_oss.chorus.entity.mob.EntityBat]s only.
     *
     * @return triggers pressure plate
     */
    open fun doesTriggerPressurePlate(): Boolean {
        return true
    }

    open fun canPassThrough(): Boolean {
        return true
    }

    protected open fun checkChunks() {
        if (this.chunk == null || (chunk!!.x != (position.x.toInt() shr 4)) || chunk!!.z != (position.z.toInt() shr 4)) {
            if (this.chunk != null) {
                chunk!!.removeEntity(this)
            }
            this.chunk = level!!.getChunk(
                position.x.toInt() shr 4,
                position.z.toInt() shr 4, true
            )

            if (!this.justCreated) {
                val newChunk: MutableMap<Int, Player> =
                    level!!.getChunkPlayers(position.x.toInt() shr 4, position.z.toInt() shr 4).toMutableMap()
                for (player: Player in ArrayList(hasSpawned.values)) {
                    if (!newChunk.containsKey(player.loaderId)) {
                        this.despawnFrom(player)
                    } else {
                        newChunk.remove(player.loaderId)
                    }
                }

                for (player: Player in newChunk.values) {
                    this.spawnTo(player)
                }
            }

            if (this.chunk == null) {
                return
            }

            chunk!!.addEntity(this)
        }
    }

    fun getMotion(): Vector3 {
        return Vector3(motion.x, motion.y, motion.z)
    }

    /**
     * 设置一个运动向量(会使得实体移动这个向量的距离，非精准移动)
     *
     *
     *
     *
     * Set a motion vector (will make the entity move the distance of this vector, not move precisely)
     *
     * @param motion 运动向量<br></br>a motion vector
     * @return boolean
     */
    open fun setMotion(motion: Vector3): Boolean {
        if (!this.justCreated) {
            val ev = EntityMotionEvent(this, motion)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            }
        }

        this.motion.x = motion.x
        this.motion.y = motion.y
        this.motion.z = motion.z

        if (!this.justCreated && !this.isImmobile()) {
            this.updateMovement()
        }

        return true
    }

    fun isOnGround(): Boolean {
        return onGround
    }

    open fun kill() {
        this.health = 0f
        this.scheduleUpdate()

        for (passenger: Entity in ArrayList(this.passengers)) {
            dismountEntity(passenger)
        }
    }

    @JvmOverloads
    fun teleport(pos: Vector3, cause: TeleportCause? = TeleportCause.PLUGIN): Boolean {
        return this.teleport(
            Transform.fromObject(
                pos,
                level!!, rotation.yaw, rotation.pitch, rotation.yaw
            ), cause
        )
    }

    @JvmOverloads
    fun teleport(pos: Locator, cause: TeleportCause? = TeleportCause.PLUGIN): Boolean {
        return this.teleport(
            Transform.fromObject(
                pos.position, pos.level,
                rotation.yaw, rotation.pitch, rotation.yaw
            ), cause
        )
    }

    fun teleport(transform: Transform): Boolean {
        return this.teleport(transform, TeleportCause.PLUGIN)
    }

    /**
     * Teleport the entity to another location
     *
     * @param transform the another location
     * @param cause    the teleported cause
     * @return the boolean
     */
    open fun teleport(transform: Transform, cause: TeleportCause?): Boolean {
        val yaw: Double = transform.rotation.yaw
        val pitch: Double = transform.rotation.pitch

        val from: Transform = this.transform
        var to: Transform = transform
        if (cause != null) {
            val ev = EntityTeleportEvent(this, from, to, cause)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            }
            to = ev.to
        }

        val currentRide: Entity? = getRiding()
        if (currentRide != null && !currentRide.dismountEntity(this)) {
            return false
        }

        this.positionChanged = true
        this.ySize = 0f

        this.setMotion(Vector3())

        if (this.setPositionAndRotation(to.locator, yaw, pitch)) {
            this.resetFallDistance()
            this.onGround = !this.noClip
            this.updateMovement()
            return true
        }

        return false
    }

    /**
     * return runtime id (changed after restart the server),the id is incremental number
     */
    fun getRuntimeID(): Long {
        return this.runtimeId
    }

    fun getUniqueID(): Long {
        return this.uniqueId
    }

    fun respawnToAll() {
        val players: Array<Player> = hasSpawned.values.toTypedArray()
        hasSpawned.clear()

        for (player: Player in players) {
            this.spawnTo(player)
        }
    }

    open fun spawnToAll() {
        if (this.chunk == null || this.closed) {
            return
        }

        for (player: Player in level!!.getChunkPlayers(
            chunk!!.x,
            chunk!!.z
        ).values) {
            if (player.isOnline) {
                this.spawnTo(player)
            }
        }
    }

    fun despawnFromAll() {
        for (player in hasSpawned.values) {
            this.despawnFrom(player)
        }
    }

    open fun close() {
        close(true)
    }

    private fun close(despawn: Boolean) {
        if (!this.closed) {
            this.closed = true

            if (despawn) {
                try {
                    val event = EntityDespawnEvent(this)

                    Server.instance.pluginManager.callEvent(event)

                    if (event.cancelled) {
                        this.closed = false
                        return
                    }
                } catch (e: Throwable) {
                    this.closed = false
                    throw e
                }
            }

            try {
                this.despawnFromAll()
            } finally {
                try {
                    if (this.chunk != null) {
                        chunk!!.removeEntity(this)
                    }
                } finally {
                    level!!.removeEntity(this)
                }
            }
        }
    }

    fun setDataProperties(maps: Map<EntityDataType<*>, Any>) {
        setDataProperties(maps, true)
    }

    fun setDataProperties(maps: Map<EntityDataType<*>, Any>, send: Boolean) {
        val sendMap = EntityDataMap()
        for (e: Map.Entry<EntityDataType<*>, Any> in maps.entries) {
            val key: EntityDataType<*> = e.key
            val value: Any = e.value
            if (getEntityDataMap().contains(key) && getEntityDataMap().get(key) == value) {
                continue
            }
            getEntityDataMap().put(key, value)
            sendMap.put(key, value)
        }
        if (send) {
            this.sendData(hasSpawned.values.toTypedArray(), sendMap)
        }
    }

    fun setDataProperty(key: EntityDataType<*>, value: Any): Boolean {
        return setDataProperty(key, value, true)
    }

    fun setDataProperty(key: EntityDataType<*>, value: Any, send: Boolean): Boolean {
        if (getEntityDataMap().contains(key) && getEntityDataMap()[key] == value) {
            return false
        }

        getEntityDataMap().put(key, value)
        if (send) {
            val map = EntityDataMap()
            map.put(key, value)
            this.sendData(hasSpawned.values.toTypedArray(), map)
        }
        return true
    }

    fun getEntityDataMap(): EntityDataMap {
        return this.entityDataMap
    }

    fun <T : Any> getDataProperty(key: EntityDataType<T>): T {
        return getDataProperty(key, key.getDefaultValue())
    }

    fun <T : Any> getDataProperty(key: EntityDataType<T>, d: T): T {
        return getEntityDataMap().getOrDefault(key, d) as T
    }

    fun setDataFlag(entityFlag: EntityFlag) {
        this.setDataFlag(entityFlag, true)
    }

    fun setDataFlag(entityFlag: EntityFlag, value: Boolean) {
        if (getEntityDataMap().existFlag(entityFlag) xor value) {
            getEntityDataMap().setFlag(entityFlag, value)
            val entityDataMap = EntityDataMap()
            entityDataMap[EntityDataTypes.FLAGS] = getEntityDataMap().getFlags()
            sendData(hasSpawned.values.toTypedArray(), entityDataMap)
        }
    }

    fun setDataFlags(entityFlags: EnumSet<EntityFlag>) {
        getEntityDataMap()[EntityDataTypes.FLAGS] = entityFlags
        sendData(hasSpawned.values.toTypedArray(), entityDataMap)
    }

    fun setDataFlagExtend(entityFlag: EntityFlag) {
        this.setDataFlag(entityFlag, true)
    }

    fun setDataFlagExtend(entityFlag: EntityFlag, value: Boolean) {
        if (getEntityDataMap().existFlag(entityFlag) xor value) {
            val entityFlags: EnumSet<EntityFlag> =
                getEntityDataMap().getOrDefault(
                    EntityDataTypes.FLAGS_2, EnumSet.noneOf(
                        EntityFlag::class.java
                    )
                ) as EnumSet<EntityFlag>
            if (value) {
                entityFlags.add(entityFlag)
            } else {
                entityFlags.remove(entityFlag)
            }
            getEntityDataMap()[EntityDataTypes.FLAGS_2] = entityFlags
            val entityDataMap = EntityDataMap()
            entityDataMap[EntityDataTypes.FLAGS_2] = entityFlags
            sendData(hasSpawned.values.toTypedArray(), entityDataMap)
        }
    }

    fun setDataFlagsExtend(entityFlags: EnumSet<EntityFlag>) {
        getEntityDataMap().put(EntityDataTypes.FLAGS_2, entityFlags)
        sendData(hasSpawned.values.toTypedArray(), entityDataMap)
    }

    fun getDataFlag(id: EntityFlag?): Boolean {
        return getEntityDataMap().getOrCreateFlags().contains(id)
    }

    fun setPlayerFlag(entityFlag: PlayerFlag) {
        var flags: Byte = getEntityDataMap().getOrDefault(EntityDataTypes.PLAYER_FLAGS, 0.toByte()) as Byte
        flags = (flags.toInt() xor (1 shl entityFlag.getValue()).toByte().toInt()).toByte()
        this.setDataProperty(EntityDataTypes.PLAYER_FLAGS, flags)
    }


    open fun isUndead(): Boolean {
        return false
    }


    fun isInEndPortal(): Boolean {
        return inEndPortal
    }


    open fun isPreventingSleep(player: Player?): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (javaClass != other.javaClass) {
            return false
        }
        return this.getRuntimeID() == (other as Entity).getRuntimeID()
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = (29 * hash + this.getRuntimeID()).toInt()
        return hash
    }


    fun isSpinAttacking(): Boolean {
        return this.getDataFlag(EntityFlag.DAMAGE_NEARBY_MOBS)
    }


    fun setSpinAttacking(value: Boolean) {
        this.setDataFlag(EntityFlag.DAMAGE_NEARBY_MOBS, value)
    }


    fun setSpinAttacking() {
        this.setSpinAttacking(true)
    }


    fun isNoClip(): Boolean {
        return noClip
    }


    fun setNoClip(noClip: Boolean) {
        this.noClip = noClip
        this.setDataFlag(EntityFlag.HAS_COLLISION, noClip)
    }


    open fun isBoss(): Boolean {
        return this is EntityBoss
    }


    fun addTag(tag: String) {
        namedTag!!.putList(
            "Tags",
            namedTag!!.getList("Tags", StringTag::class.java).add(StringTag(tag))
        )
    }


    fun removeTag(tag: String) {
        val tags: ListTag<StringTag> = namedTag!!.getList("Tags", StringTag::class.java)
        tags.remove(StringTag(tag))
        namedTag!!.putList("Tags", tags)
    }


    fun containTag(tag: String): Boolean {
        return namedTag!!.getList(
            "Tags",
            StringTag::class.java
        ).all.stream().anyMatch { t: StringTag -> t.data == tag }
    }


    fun getAllTags(): List<StringTag> {
        return namedTag!!.getList("Tags", StringTag::class.java).all
    }


    fun getFreezingEffectStrength(): Float {
        return this.getDataProperty(EntityDataTypes.FREEZING_EFFECT_STRENGTH)
    }


    fun setFreezingEffectStrength(strength: Float) {
        require(!(strength < 0 || strength > 1)) { "Freezing Effect Strength must be between 0 and 1" }
        this.setDataProperty(EntityDataTypes.FREEZING_EFFECT_STRENGTH, strength)
    }


    fun getFreezingTicks(): Int {
        return this.freezingTicks
    }


    fun setFreezingTicks(ticks: Int) {
        this.freezingTicks = max(0.0, min(ticks.toDouble(), 140.0)).toInt()
        setFreezingEffectStrength(ticks / 140f)
    }


    fun addFreezingTicks(increments: Int) {
        if (freezingTicks + increments < 0) this.freezingTicks = 0
        else if (freezingTicks + increments > 140) this.freezingTicks = 140
        else this.freezingTicks += increments
        setFreezingEffectStrength(this.freezingTicks / 140f)
    }


    fun setAmbientSoundInterval(interval: Float) {
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_INTERVAL, interval)
    }


    fun setAmbientSoundIntervalRange(range: Float) {
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_INTERVAL, range)
    }


    fun setAmbientSoundEvent(sound: Sound) {
        this.setAmbientSoundEventName(sound.sound)
    }


    fun setAmbientSoundEventName(eventName: String) {
        this.setDataProperty(EntityDataTypes.AMBIENT_SOUND_EVENT_NAME, eventName)
    }


    fun playActionAnimation(action: AnimatePacket.Action, rowingTime: Float) {
        val viewers: HashSet<Player> = HashSet(viewers.values)
        if (this.isPlayer) viewers.add(this as Player)
        playActionAnimation(action, rowingTime, viewers)
    }

    /**
     * Play the action animation of this entity to a specified group of players
     *
     *
     * 向指定玩家群体播放此实体的action动画
     *
     * @param action     the action
     * @param rowingTime the rowing time
     * @param players    可视玩家 Visible Player
     */
    fun playActionAnimation(action: AnimatePacket.Action, rowingTime: Float, players: Collection<Player>) {
        Server.broadcastPacket(
            players, AnimatePacket(
                action = action,
                targetRuntimeID = this.getRuntimeID(),
                actionData = AnimatePacket.Action.RowingData(
                    rowingTime = rowingTime
                )
            )
        )
    }

    fun getLookingAngleAt(location: Vector3): Double {
        val anglePosition: Double = abs(Math.toDegrees(atan2(location.x - position.x, location.z - position.z)))
        val angleVector: Double = abs(
            Math.toDegrees(
                atan2(
                    getDirectionVector().x, getDirectionVector().z
                )
            )
        )
        return abs(anglePosition - angleVector)
    }

    fun getLookingAngleAtPitch(location: Vector3): Double {
        val anglePosition: Double = abs(Math.toDegrees(atan2(location.y - (position.y + getEyeHeight()), 0.0)))
        val angleVector: Double = abs(rotation.pitch)
        return abs(abs(anglePosition - angleVector) - 90)
    }

    fun isLookingAt(location: Vector3, tolerance: Double, checkRaycast: Boolean): Boolean {
        return getLookingAngleAt(location) <= tolerance && getLookingAngleAtPitch(location) <= tolerance && (!checkRaycast || level!!.raycastBlocks(
            location,
            position.add(0.0, getEyeHeight().toDouble(), 0.0)
        ).isEmpty())
    }

    private fun validateAndSetIntProperty(identifier: String, value: Int): Boolean {
        if (!intProperties.containsKey(identifier)) return false
        intProperties[identifier] = value
        return true
    }

    fun setIntEntityProperty(identifier: String, value: Int): Boolean {
        return validateAndSetIntProperty(identifier, value)
    }


    fun setBooleanEntityProperty(identifier: String, value: Boolean): Boolean {
        return validateAndSetIntProperty(identifier, if (value) 1 else 0)
    }

    fun setFloatEntityProperty(identifier: String, value: Float): Boolean {
        if (!floatProperties.containsKey(identifier)) return false
        floatProperties[identifier] = value
        return true
    }

    fun setEnumEntityProperty(identifier: String, value: String): Boolean {
        if (!intProperties.containsKey(identifier)) return false
        val entityPropertyList: List<EntityProperty> = EntityProperty.getEntityProperty(
            this.getEntityIdentifier()
        )

        for (property: EntityProperty in entityPropertyList) {
            if (identifier != property.getIdentifier() || property !is EnumEntityProperty) {
                continue
            }
            val index: Int = property.findIndex(value)

            if (index >= 0) {
                intProperties[identifier] = index
                return true
            }
            return false
        }
        return false
    }

    fun getIntEntityProperty(identifier: String?): Int {
        return intProperties[identifier]!!
    }

    fun getBooleanEntityProperty(identifier: String?): Boolean {
        return intProperties[identifier] == 1
    }

    fun getFloatEntityProperty(identifier: String?): Float {
        return floatProperties[identifier]!!
    }

    fun getEnumEntityProperty(identifier: String): String? {
        val entityPropertyList: List<EntityProperty> = EntityProperty.getEntityProperty(
            this.getEntityIdentifier()
        )

        for (property: EntityProperty in entityPropertyList) {
            if (identifier != property.getIdentifier() || property !is EnumEntityProperty) {
                continue
            }
            return property.getEnums()[intProperties.get(identifier)!!]
        }
        return null
    }

    private fun initEntityProperties(entityIdentifier: String) {
        val entityPropertyList: List<EntityProperty> = EntityProperty.getEntityProperty(entityIdentifier)
        if (entityPropertyList.isEmpty()) return

        for (property: EntityProperty in entityPropertyList) {
            val identifier: String = property.getIdentifier()

            when (property) {
                is FloatEntityProperty -> floatProperties[identifier] = property.getDefaultValue()
                is IntEntityProperty -> intProperties[identifier] = property.getDefaultValue()
                is BooleanEntityProperty -> intProperties[identifier] = if (property.getDefaultValue()) 1 else 0
                is EnumEntityProperty -> intProperties[identifier] = property.findIndex(property.getDefaultValue())
                else -> Unit
            }
        }
    }

    fun propertySyncData(): PropertySyncData {
        val intArray = intProperties.values.toIntArray()
        val floatArray = floatProperties.values.toFloatArray()

        return PropertySyncData(intArray, floatArray)
    }

    fun getAttributes(): MutableMap<Int, Attribute> {
        return attributes
    }

    fun isInitialized(): Boolean {
        return initialized
    }

    fun getLevel(): Level {
        return level!!
    }

    fun getChunkX(): Int {
        return position.chunkX
    }

    fun getChunkZ(): Int {
        return position.chunkZ
    }

    fun getX(): Double {
        return position.x
    }

    fun getY(): Double {
        return position.y
    }

    fun getZ(): Double {
        return position.z
    }

    override val vector3: Vector3
        get() {
            return position.clone()
        }

    companion object : Loggable {
        const val TAG_CHESTED: String = "Chested"
        const val TAG_COLOR: String = "Color"
        const val TAG_COLOR2: String = "Color2"
        const val TAG_CUSTOM_NAME: String = "CustomName"
        const val TAG_CUSTOM_NAME_VISIBLE: String = "CustomNameVisible"
        const val TAG_DEFINITIONS: String = "definitions"
        const val TAG_FALL_DISTANCE: String = "FallDistance"
        const val TAG_FIRE: String = "Fire"
        const val TAG_IDENTIFIER: String = "identifier"
        const val TAG_INTERNAL_COMPONENTS: String = "InternalComponents"
        const val TAG_INVULNERABLE: String = "Invulnerable"
        const val TAG_IS_ANGRY: String = "IsAngry"
        const val TAG_IS_AUTONOMOUS: String = "IsAutonomous"
        const val TAG_IS_BABY: String = "IsBaby"
        const val TAG_IS_EATING: String = "IsEating"
        const val TAG_IS_GLIDING: String = "IsGliding"
        const val TAG_IS_GLOBAL: String = "IsGlobal"
        const val TAG_IS_ILLAGER_CAPTAIN: String = "IsIllagerCaptain"
        const val TAG_IS_ORPHANED: String = "IsOrphaned"
        const val TAG_IS_OUT_OF_CONTROL: String = "IsOutOfControl"
        const val TAG_IS_ROARING: String = "IsRoaring"
        const val TAG_IS_SCARED: String = "IsScared"
        const val TAG_IS_STUNNED: String = "IsStunned"
        const val TAG_IS_SWIMMING: String = "IsSwimming"
        const val TAG_IS_TAMED: String = "IsTamed"
        const val TAG_IS_TRUSTING: String = "IsTrusting"
        const val TAG_LAST_DIMENSION_ID: String = "LastDimensionId"
        const val TAG_LINKS_TAG: String = "LinksTag"
        const val TAG_LOOT_DROPPED: String = "LootDropped"
        const val TAG_MARK_VARIANT: String = "MarkVariant"
        const val TAG_MOTION: String = "Motion"
        const val TAG_ON_GROUND: String = "OnGround"
        const val TAG_OWNER_NEW: String = "OwnerNew"
        const val TAG_PERSISTENT: String = "Persistent"
        const val TAG_PORTAL_COOLDOWN: String = "PortalCooldown"
        const val TAG_POS: String = "Pos"
        const val TAG_ROTATION: String = "Rotation"
        const val TAG_SADDLED: String = "Saddled"
        const val TAG_SHEARED: String = "Sheared"
        const val TAG_SHOW_BOTTOM: String = "ShowBottom"
        const val TAG_SKIN_ID: String = "SkinID"
        const val TAG_STRENGTH: String = "Strength"
        const val TAG_STRENGTH_MAX: String = "StrengthMax"
        const val TAG_TAGS: String = "Tags"
        const val TAG_UNIQUE_ID: String = "UniqueID"
        const val TAG_VARIANT: String = "Variant"

        @JvmField
        val EMPTY_ARRAY: Array<Entity> = emptyArray()
        var entityCount: AtomicLong = AtomicLong(1)

        /**
         * 从mc标准实体标识符创建实体，形如(minecraft:sheep)
         *
         *
         * Create an entity from the entity identifier, like (minecraft:sheep)
         *
         * @param identifier the identifier
         * @param pos        the pos
         * @param args       the args
         * @return the entity
         */
        fun createEntity(identifier: Identifier, pos: Locator, vararg args: Any?): Entity? {
            return createEntity(
                identifier.toString(),
                requireNonNull(pos.chunk)!!,
                getDefaultNBT(pos.position),
                *args
            )
        }

        /**
         * 创建一个实体从实体名,名称从[registerEntities][Entity.init]源代码查询
         *
         *
         * Create an entity from entity name, name from [registerEntities][Entity.init] source code query
         *
         * @param name the name
         * @param pos  the pos
         * @param args the args
         * @return the entity
         */
        @JvmStatic
        fun createEntity(name: String, pos: Locator, vararg args: Any?): Entity? {
            return createEntity(name, requireNonNull(pos.chunk)!!, getDefaultNBT(pos.position), *args)
        }

        /**
         * 创建一个实体从网络id
         *
         *
         * Create an entity from the network id
         *
         * @param type 网络ID<br></br> network id
         * @param pos  the pos
         * @param args the args
         * @return the entity
         */
        @JvmStatic
        fun createEntity(type: Int, pos: Locator, vararg args: Any?): Entity? {
            val entityIdentifier = Registries.ENTITY.getEntityIdentifier(type)
            return createEntity(
                entityIdentifier,
                pos.chunk,
                getDefaultNBT(pos.position),
                *args
            )
        }

        /**
         * 创建一个实体从网络id
         *
         *
         * Create an entity from the network id
         *
         * @param type  网络ID<br></br> network id
         * @param chunk the chunk
         * @param nbt   the nbt
         * @param args  the args
         * @return the entity
         */
        @JvmStatic
        fun createEntity(type: Int, chunk: IChunk, nbt: CompoundTag, vararg args: Any?): Entity? {
            val entityIdentifier = Registries.ENTITY.getEntityIdentifier(type)
            return Registries.ENTITY.provideEntity(entityIdentifier, chunk, nbt, *args)
        }

        @JvmStatic
        fun createEntity(identifier: String, chunk: IChunk, nbt: CompoundTag, vararg args: Any?): Entity? {
            Identifier.assertValid(identifier)
            return Registries.ENTITY.provideEntity(identifier, chunk, nbt, *args)
        }

        /**
         * 获取指定网络id实体的标识符
         *
         *
         * Get the identifier of the specified network id entity
         *
         * @return the identifier
         */
        fun getIdentifier(networkID: Int): Identifier {
            val entityIdentifier = Registries.ENTITY.getEntityIdentifier(networkID)
            return Identifier(entityIdentifier)
        }

        /**
         * @see .getDefaultNBT
         */
        @JvmStatic
        fun getDefaultNBT(pos: Vector3): CompoundTag {
            return getDefaultNBT(pos, null)
        }

        fun getDefaultNBT(pos: Vector3, motion: Vector3?): CompoundTag {
            return getDefaultNBT(pos, motion, 0f, 0f)
        }

        /**
         * 获得该实体的默认NBT，带有位置,motion，yaw pitch等信息
         *
         *
         * Get the default NBT of the entity, with information such as position, motion, yaw pitch, etc.
         *
         * @param pos    the pos
         * @param motion the motion
         * @param yaw    the yaw
         * @param pitch  the pitch
         * @return the default nbt
         */
        @JvmStatic
        fun getDefaultNBT(pos: Vector3, motion: Vector3?, yaw: Float, pitch: Float): CompoundTag {
            return CompoundTag()
                .putList(
                    TAG_POS, ListTag<FloatTag>()
                        .add(FloatTag(pos.x.toFloat()))
                        .add(FloatTag(pos.y.toFloat()))
                        .add(FloatTag(pos.z.toFloat()))
                )
                .putList(
                    TAG_MOTION, ListTag<FloatTag>()
                        .add(FloatTag(motion?.x?.toFloat() ?: 0f))
                        .add(FloatTag(motion?.y?.toFloat() ?: 0f))
                        .add(FloatTag(motion?.z?.toFloat() ?: 0f))
                )
                .putList(
                    TAG_ROTATION, ListTag<FloatTag>()
                        .add(FloatTag(yaw))
                        .add(FloatTag(pitch))
                )
        }

        fun playAnimationOnEntities(
            animation: org.chorus_oss.protocol.packets.AnimateEntityPacket,
            entities: List<Entity>
        ) {
            val viewers =
                entities.flatMap { it.viewers.values + if (it.isPlayer) listOf(it as Player) else emptyList() }.toSet()
            Server.broadcastPacket(viewers, animation)
        }
    }
}
