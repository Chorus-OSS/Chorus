package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.EntityEquipment
import org.chorus_oss.chorus.entity.EntityPhysical
import org.chorus_oss.chorus.entity.ai.EntityAI
import org.chorus_oss.chorus.entity.ai.behaviorgroup.EmptyBehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.EntityControlUtils
import org.chorus_oss.chorus.entity.ai.evaluator.LogicalUtils
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage
import org.chorus_oss.chorus.entity.ai.memory.IMemoryType
import org.chorus_oss.chorus.entity.mob.monster.EntityCreeper
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.form.window.SimpleForm
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.network.protocol.MoveEntityDeltaPacket
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow

abstract class EntityMob(chunk: IChunk?, nbt: CompoundTag) : EntityPhysical(chunk, nbt), EntityInventoryHolder,
    EntityCanAttack, LogicalUtils, EntityControlUtils {
    var activeEffects: List<CompoundTag>? = null
    var air: Short = 0
    override var attackTime: Short = 0

    //    @NotNull public List<CompoundTag> attributes = new ArrayList<>();
    var bodyRot: Float? = null
    var boundX: Int = 0
    var boundY: Int = 0
    var boundZ: Int = 0
    var canPickupItems: Boolean = false
    var dead: Boolean = false
    var deathTime: Short = 0
    var hasBoundOrigin: Boolean = false
    var hasSetCanPickupItems: Boolean = false
    var hurtTime: Short = 0
    var leasherID: Long = 0L
    var limitedLife: Long = 0L
    var naturalSpawn: Boolean = false
    var persistingOffers: CompoundTag? = null
    var persistingRiches: Int? = null
    var surface: Boolean = true
    var targetCaptainID: Long? = null
    var targetID: Long = 0L
    var tradeExperience: Int? = null
    open var tradeTier: Int? = null
    var wantsToBeJockey: Boolean? = null

    @JvmField
    var headYaw: Double = rotation.yaw

    @JvmField
    var prevHeadYaw: Double = headYaw

    /**
     * 不同难度下实体空手能造成的伤害.
     *
     *
     * The damage that can be caused by the entity's empty hand at different difficulties.
     */
    override lateinit var diffHandDamage: FloatArray


    override val equipment = EntityEquipment(this)

    override val memoryStorage: IMemoryStorage
        get() = this.behaviorGroup.memoryStorage


    override fun initEntity() {
        super.initEntity()

        this.behaviorGroup = requireBehaviorGroup()

        memoryStorage[CoreMemoryTypes.ENTITY_SPAWN_TIME] = level!!.tick
        IMemoryType.PERSISTENT_MEMORIES.forEach(Consumer { memory ->
            val mem = memory as IMemoryType<Any?>
            val data = mem.codec?.decoder?.apply(this.namedTag!!)
            if (data != null) {
                memoryStorage[mem] = data
            }
        })
    }

    override fun spawnToAll() {
        if (this.chunk != null && !this.closed) {
            val chunkPlayers: Collection<Player> = level!!.getChunkPlayers(
                chunk!!.x, chunk!!.z
            ).values
            for (chunkPlayer in chunkPlayers) {
                this.spawnTo(chunkPlayer)
            }
        }
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        equipment.sendContents(player)
    }

    override fun saveNBT() {
        super.saveNBT()
        behaviorGroup.save(this)

        if (activeEffects != null) namedTag!!.putList(TAG_ACTIVE_EFFECTS, ListTag(activeEffects!!))
        namedTag!!.putShort(TAG_AIR, getAirTicks())
        namedTag!!.putList(
            TAG_ARMOR, ListTag(
                Tag.TAG_COMPOUND.toInt(),
                equipment.getArmor().stream().map { NBTIO.putItemHelper(it) }.toList()
            )
        )
        namedTag!!.putShort(TAG_ATTACK_TIME, attackTime.toInt())
        //        this.namedTag.putList(TAG_ATTRIBUTES, new ListTag<>(Tag.TAG_Compound, this.attributes));
        if (bodyRot != null) namedTag!!.putFloat(TAG_BODY_ROT, bodyRot!!)
        namedTag!!.putInt(TAG_BOUND_X, boundX)
        namedTag!!.putInt(TAG_BOUND_Y, boundY)
        namedTag!!.putInt(TAG_BOUND_Z, boundZ)
        namedTag!!.putBoolean(TAG_CAN_PICKUP_ITEMS, canPickupItems)
        namedTag!!.putBoolean(TAG_DEAD, dead)
        namedTag!!.putShort(TAG_DEATH_TIME, deathTime.toInt())
        namedTag!!.putBoolean(TAG_HAS_BOUND_ORIGIN, hasBoundOrigin)
        namedTag!!.putBoolean(TAG_HAS_SET_CAN_PICKUP_ITEMS, hasSetCanPickupItems)
        namedTag!!.putShort(TAG_HURT_TIME, hurtTime.toInt())
        namedTag!!.putLong(TAG_LEASHER_ID, leasherID)
        namedTag!!.putLong(TAG_LIMITED_LIFE, limitedLife)
        namedTag!!.putList(
            TAG_MAINHAND, ListTag(
                Tag.TAG_COMPOUND.toInt(), listOf(
                    NBTIO.putItemHelper(
                        equipment.getMainHand()
                    )
                )
            )
        )
        namedTag!!.putBoolean(TAG_NATURAL_SPAWN, naturalSpawn)
        namedTag!!.putList(
            TAG_OFFHAND, ListTag(
                Tag.TAG_COMPOUND.toInt(), listOf(
                    NBTIO.putItemHelper(
                        equipment.getOffHand()
                    )
                )
            )
        )
        if (persistingOffers != null) namedTag!!.putCompound(TAG_PERSISTING_OFFERS, this.persistingOffers!!)
        if (persistingRiches != null) namedTag!!.putInt(
            TAG_PERSISTING_RICHES,
            persistingRiches!!
        )
        namedTag!!.putBoolean(TAG_SURFACE, this.surface)
        if (targetCaptainID != null) namedTag!!.putLong(
            TAG_TARGET_CAPTAIN_ID,
            targetCaptainID!!
        )
        namedTag!!.putLong(TAG_TARGET_ID, targetID)
        if (tradeExperience != null) namedTag!!.putInt(
            TAG_TRADE_EXPERIENCE,
            tradeExperience!!
        )
        if (tradeTier != null) namedTag!!.putInt(TAG_TRADE_TIER, tradeTier!!)
        if (wantsToBeJockey != null) namedTag!!.putBoolean(
            TAG_WANTS_TO_BE_JOCKEY,
            wantsToBeJockey!!
        )
    }

    open fun getAdditionalArmor(): Int {
        return 0
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        val storage = memoryStorage
        if (storage != null) {
            storage.set(CoreMemoryTypes.BE_ATTACKED_EVENT, source)
            storage.set(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, level!!.tick)
        }

        if (this.isClosed() || !this.isAlive()) {
            return false
        }

        if (source is EntityDamageByEntityEvent && source.damager !is EntityCreeper) {
            //更新仇恨目标
            memoryStorage.set(CoreMemoryTypes.ATTACK_TARGET, source.damager)
        }

        if (source.cause != DamageCause.VOID && source.cause != DamageCause.CUSTOM && source.cause != DamageCause.MAGIC && source.cause != DamageCause.HUNGER) {
            var armorPoints = getAdditionalArmor()
            var epf = 0

            //            int toughness = 0;
            for (armor in equipment.getArmor()) {
                armorPoints += armor.armorPoints
                epf += calculateEnchantmentProtectionFactor(armor, source).toInt()
                //toughness += armor.getToughness();
            }

            if (source.canBeReducedByArmor()) {
                source.setDamage(-source.finalDamage * armorPoints * 0.04f, DamageModifier.ARMOR)
            }

            source.setDamage(
                (-source.finalDamage * min(
                    ceil(
                        (min(
                            epf.toDouble(),
                            25.0
                        ) * (ThreadLocalRandom.current().nextInt(50, 100).toFloat() / 100)).toFloat()
                    ).toDouble(), 20.0
                ) * 0.04f).toFloat(),
                DamageModifier.ARMOR_ENCHANTMENTS
            )

            source.setDamage(
                (-min(getAbsorption().toDouble(), source.finalDamage.toDouble())).toFloat(),
                DamageModifier.ABSORPTION
            )
        }

        if (super.attack(source)) {
            val damager = if (source is EntityDamageByEntityEvent) {
                source.damager
            } else null

            val damagedArmor = equipment.getArmor().stream().map { damageArmor(it, damager) }.toList()
            equipment.setArmor(damagedArmor)

            return true
        } else {
            return false
        }
    }

    protected open fun calculateEnchantmentProtectionFactor(item: Item, source: EntityDamageEvent): Double {
        if (!item.hasEnchantments()) {
            return 0.0
        }

        var epf = 0.0

        if (item.applyEnchantments()) {
            for (ench in item.enchantments) {
                epf += ench.getProtectionFactor(source).toDouble()
            }
        }

        return epf
    }

    protected fun damageArmor(armor: Item, damager: Entity?): Item {
        if (armor.hasEnchantments()) {
            if (damager != null) {
                if (armor.applyEnchantments()) {
                    for (enchantment in armor.enchantments) {
                        enchantment.doPostAttack(damager, this)
                    }
                }
            }

            val durability = armor.getEnchantment(Enchantment.ID_DURABILITY)
            if (durability != null && durability.level > 0 && (100 / (durability.level + 1)) <= Utils.random.nextInt(100)) {
                return armor
            }
        }

        if (armor.isUnbreakable || armor.maxDurability < 0) {
            return armor
        }

        armor.damage += 1

        if (armor.damage >= armor.maxDurability) {
            level!!.addSound(this.position, Sound.RANDOM_BREAK)
            return Item.get(BlockID.AIR, 0, 0)
        }

        return armor
    }

    override val inventory: Inventory = this.equipment

    override fun attackTarget(entity: Entity): Boolean {
        return entity is Player
    }

    override fun getDrops(): Array<Item> {
        return inventory.contents.values.stream()
            .filter { it.hasEnchantment(Enchantment.ID_VANISHING_CURSE) }
            .toList().toTypedArray()
    }

    override fun hasRotationChanged(threshold: Double): Boolean {
        return ((if (enableHeadYaw()) (this.headYaw - this.prevHeadYaw).pow(2.0) else 0.0) + rotation.subtract(this.prevRotation)
            .lengthSquared()) > threshold
    }

    override fun moveDelta() {
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
        if (!prevHeadYaw.equals(this.headYaw)) {
            pk.headYaw = headYaw.toFloat()
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_HAS_HEAD_YAW.toInt()).toShort()
        }
        if (this.onGround) {
            pk.flags = (pk.flags.toInt() or MoveEntityDeltaPacket.FLAG_ON_GROUND.toInt()).toShort()
        }
        Server.broadcastPacket(this.viewers.values, pk)
    }

    fun enableHeadYaw(): Boolean {
        return true
    }

    fun setRotation(yaw: Double, pitch: Double, headYaw: Double) {
        rotation.yaw = yaw
        rotation.pitch = pitch
        this.headYaw = headYaw
        this.scheduleUpdate()
    }

    fun setPositionAndRotation(pos: Vector3, yaw: Double, pitch: Double, headYaw: Double): Boolean {
        this.setRotation(yaw, pitch, headYaw)
        return this.setPosition(pos)
    }

    fun setPositionAndRotation(pos: Locator, yaw: Double, pitch: Double, headYaw: Double): Boolean {
        this.setRotation(yaw, pitch, headYaw)
        return this.setPosition(pos)
    }

    override fun getExperienceDrops(): Int {
        return 5
    }

    lateinit var behaviorGroup: IBehaviorGroup
        private set

    /**
     * 是否为活跃实体，如果实体不活跃，就应当降低AI运行频率
     */

    var isActive: Boolean = true

    init {
        if (nbt.contains(TAG_ACTIVE_EFFECTS)) this.activeEffects = nbt.getList(
            TAG_ACTIVE_EFFECTS,
            CompoundTag::class.java
        ).all
        equipment.setArmor(
            Stream.concat(
                nbt.getList(
                    TAG_ARMOR,
                    CompoundTag::class.java
                ).all.stream(), Stream.generate<CompoundTag?> { null }).limit(4)
                .map { tag: CompoundTag? -> NBTIO.getItemHelper(tag) }.toList()
        )
        this.attackTime = nbt.getShort(TAG_ATTACK_TIME)
        //        this.attributes = nbt.getList(TAG_ATTRIBUTES, CompoundTag.class).getAll();
        if (nbt.contains(TAG_BODY_ROT)) this.bodyRot = nbt.getFloat(TAG_BODY_ROT)
        this.boundX = nbt.getInt(TAG_BOUND_X)
        this.boundY = nbt.getInt(TAG_BOUND_Y)
        this.boundZ = nbt.getInt(TAG_BOUND_Z)
        this.canPickupItems = nbt.getBoolean(TAG_CAN_PICKUP_ITEMS)
        this.dead = nbt.getBoolean(TAG_DEAD)
        this.deathTime = nbt.getShort(TAG_DEATH_TIME)
        this.hasBoundOrigin = nbt.getBoolean(TAG_HAS_BOUND_ORIGIN)
        this.hasSetCanPickupItems = nbt.getBoolean(TAG_HAS_SET_CAN_PICKUP_ITEMS)
        this.hurtTime = nbt.getShort(TAG_HURT_TIME)
        this.leasherID = nbt.getLong(TAG_LEASHER_ID)
        this.limitedLife = nbt.getLong(TAG_LIMITED_LIFE)
        equipment.setMainHand(
            Stream.concat(
                nbt.getList(
                    TAG_MAINHAND,
                    CompoundTag::class.java
                ).all.stream(), Stream.generate<CompoundTag?> { null }).limit(1)
                .map { NBTIO.getItemHelper(it) }.toList().first()
        )
        this.naturalSpawn = nbt.getBoolean(TAG_NATURAL_SPAWN)
        equipment.setOffHand(
            Stream.concat(
                nbt.getList(
                    TAG_OFFHAND,
                    CompoundTag::class.java
                ).all.stream(), Stream.generate<CompoundTag?> { null }).limit(1)
                .map { NBTIO.getItemHelper(it) }.toList().first()
        )
        if (nbt.contains(TAG_PERSISTING_OFFERS)) this.persistingOffers = nbt.getCompound(TAG_PERSISTING_OFFERS)
        if (nbt.contains(TAG_PERSISTING_RICHES)) this.persistingRiches = nbt.getInt(TAG_PERSISTING_RICHES)
        this.surface = nbt.getBoolean(TAG_SURFACE)
        if (nbt.contains(TAG_TARGET_CAPTAIN_ID)) this.targetCaptainID = nbt.getLong(TAG_TARGET_CAPTAIN_ID)
        this.targetID = nbt.getLong(TAG_TARGET_ID)
        if (nbt.contains(TAG_TRADE_EXPERIENCE)) this.tradeExperience = nbt.getInt(TAG_TRADE_EXPERIENCE)
        if (nbt.contains(TAG_TRADE_TIER)) this.tradeTier = nbt.getInt(TAG_TRADE_TIER)
        if (nbt.contains(TAG_WANTS_TO_BE_JOCKEY)) this.wantsToBeJockey = nbt.getBoolean(TAG_WANTS_TO_BE_JOCKEY)
    }

    /**
     * 请求一个行为组实例，此方法在实体初始化行为组时调用
     *
     * @return 新创建的行为组
     */
    protected open fun requireBehaviorGroup(): IBehaviorGroup {
        return EmptyBehaviorGroup(this)
    }

    override fun asyncPrepare(currentTick: Int) {
        if (!isAlive()) return
        // 计算是否活跃
        isActive = level!!.isHighLightChunk(getChunkX(), getChunkZ())
        if (!this.isImmobile()) { // immobile会禁用实体AI
            behaviorGroup.collectSensorData(this)
            behaviorGroup.evaluateCoreBehaviors(this)
            behaviorGroup.evaluateBehaviors(this)
            behaviorGroup.tickRunningCoreBehaviors(this)
            behaviorGroup.tickRunningBehaviors(this)
            behaviorGroup.updateRoute(this)
            behaviorGroup.applyController(this)
            if (EntityAI.hasDebugOptions()) behaviorGroup.debugTick(this)
        }
        super.asyncPrepare(currentTick)
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (!EntityAI.checkDebugOption(EntityAI.DebugOption.MEMORY)) {
            return super.onInteract(player, item, clickedPos)
        } else {
            if (player.isOp && item.id == ItemID.STICK) {
                val strBuilder = StringBuilder()

                //Build memory information
                strBuilder.append("§eMemory:§f\n")
                val all = memoryStorage.all
                all.forEach { (memory, value) ->
                    strBuilder.append(memory.identifier)
                    strBuilder.append(" = §b")
                    strBuilder.append(value)
                    strBuilder.append("§f\n")
                }

                val form = SimpleForm("§f${this.getOriginalName()}", strBuilder.toString())
                form.send(player)
                return true
            } else return super.onInteract(player, item, clickedPos)
        }
    }

    /**
     * 返回实体在跳跃时要增加的motion y
     *
     * @param jumpY 跳跃的高度
     * @return 实体要增加的motion y
     */
    fun getJumpingMotion(jumpY: Double): Double {
        return if (this.isTouchingWater()) {
            0.1
        } else {
            if (jumpY > 0 && jumpY < 0.2) {
                0.15
            } else if (jumpY < 0.51) {
                0.35
            } else if (jumpY < 1.01) {
                0.5
            } else {
                0.6
            }
        }
    }

    companion object {
        private const val TAG_ACTIVE_EFFECTS = "ActiveEffects"
        private const val TAG_AIR = "Air"
        private const val TAG_ARMOR = "Armor"
        private const val TAG_ATTACK_TIME = "AttackTime"
        private const val TAG_ATTRIBUTES = "Attributes"
        private const val TAG_BODY_ROT = "BodyRot"
        private const val TAG_BOUND_X = "boundX"
        private const val TAG_BOUND_Y = "boundY"
        private const val TAG_BOUND_Z = "boundZ"
        private const val TAG_CAN_PICKUP_ITEMS = "canPickupItems"
        private const val TAG_DEAD = "Dead"
        private const val TAG_DEATH_TIME = "DeathTime"
        private const val TAG_HAS_BOUND_ORIGIN = "hasBoundOrigin"
        private const val TAG_HAS_SET_CAN_PICKUP_ITEMS = "hasSetCanPickupItems"
        private const val TAG_HURT_TIME = "HurtTime"
        private const val TAG_LEASHER_ID = "LeasherID"
        private const val TAG_LIMITED_LIFE = "limitedLife"
        private const val TAG_MAINHAND = "Mainhand"
        private const val TAG_NATURAL_SPAWN = "NaturalSpawn"
        private const val TAG_OFFHAND = "Offhand"
        private const val TAG_PERSISTING_OFFERS = "persistingOffers"
        private const val TAG_PERSISTING_RICHES = "persistingRiches"
        private const val TAG_SURFACE = "Surface"
        private const val TAG_TARGET_CAPTAIN_ID = "TargetCaptainID"
        private const val TAG_TARGET_ID = "TargetID"
        private const val TAG_TRADE_EXPERIENCE = "TradeExperience"
        private const val TAG_TRADE_TIER = "TradeTier"
        private const val TAG_WANTS_TO_BE_JOCKEY = "WantsToBeJockey"
    }
}
