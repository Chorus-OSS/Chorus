package org.chorus_oss.chorus.entity.projectile.abstract_arrow

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.weather.EntityLightningBolt
import org.chorus_oss.chorus.event.entity.EntityDamageByChildEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.ProjectileHitEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.MovingObjectPosition
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.DoubleTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.concurrent.ThreadLocalRandom

class EntityThrownTrident @JvmOverloads constructor(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntityAbstractArrow(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.THROWN_TRIDENT
    }

    var alreadyCollided: Boolean = false
    protected lateinit var trident: Item

    var pickupMode: Int = 0
    private var collisionPos: Vector3? = null
    private var stuckToBlockPos: BlockVector3? = null
    private var favoredSlot: Int = 0

    // Enchantment
    private var loyaltyLevel: Int = 0
    private var hasChanneling: Boolean = false
    private var riptideLevel: Int = 0
    private var impalingLevel: Int = 0

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.05f
    }

    public override fun getDrag(): Float {
        return 0.01f
    }

    override fun getOriginalName(): String {
        return NAME_TRIDENT
    }

    override fun initEntity() {
        super.setHasAge(false)
        super.initEntity()

        this.closeOnCollide = false

        this.pickupMode =
            (if (namedTag!!.contains(TAG_PICKUP)) namedTag!!.getByte(TAG_PICKUP) else PICKUP_ANY.toByte()).toInt()
        this.favoredSlot = if (namedTag!!.contains(TAG_FAVORED_SLOT)) namedTag!!.getInt(TAG_FAVORED_SLOT) else -1
        this.player = !namedTag!!.contains(TAG_PLAYER) || namedTag!!.getBoolean(TAG_PLAYER)

        if (namedTag!!.contains(TAG_CREATIVE)) {
            if (pickupMode == PICKUP_ANY && namedTag!!.getBoolean(TAG_CREATIVE)) {
                pickupMode = PICKUP_CREATIVE
            }
            namedTag!!.remove(TAG_CREATIVE)
        }

        if (namedTag!!.contains(TAG_TRIDENT)) {
            this.trident = NBTIO.getItemHelper(namedTag!!.getCompound(TAG_TRIDENT))
            this.loyaltyLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_LOYALTY)
            this.hasChanneling = trident.hasEnchantment(Enchantment.ID_TRIDENT_CHANNELING)
            this.riptideLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_RIPTIDE)
            this.impalingLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_IMPALING)
        } else {
            this.trident = Item.AIR
            this.loyaltyLevel = 0
            this.hasChanneling = false
            this.riptideLevel = 0
            this.impalingLevel = 0
        }

        if (namedTag!!.contains("CollisionPos")) {
            val collisionPosList: ListTag<DoubleTag> = namedTag!!.getList(
                "CollisionPos",
                DoubleTag::class.java
            )
            collisionPos =
                Vector3(collisionPosList.get(0).data, collisionPosList.get(1).data, collisionPosList.get(2).data)
        } else {
            collisionPos = defaultCollisionPos.clone()
        }

        if (namedTag!!.contains("StuckToBlockPos")) {
            val stuckToBlockPosList: ListTag<IntTag> = namedTag!!.getList(
                "StuckToBlockPos",
                IntTag::class.java
            )
            stuckToBlockPos = BlockVector3(
                stuckToBlockPosList.get(0).data,
                stuckToBlockPosList.get(1).data,
                stuckToBlockPosList.get(2).data
            )
        } else {
            stuckToBlockPos = defaultStuckToBlockPos.clone()
        }
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.put(TAG_TRIDENT, NBTIO.putItemHelper(this.trident))
        namedTag!!.putByte(TAG_PICKUP, this.pickupMode)
        namedTag!!.putList(
            "CollisionPos", ListTag<DoubleTag>()
                .add(DoubleTag(collisionPos!!.x))
                .add(DoubleTag(collisionPos!!.y))
                .add(DoubleTag(collisionPos!!.z))
        )
        namedTag!!.putList(
            "StuckToBlockPos", ListTag<IntTag>()
                .add(IntTag(stuckToBlockPos!!.x))
                .add(IntTag(stuckToBlockPos!!.y))
                .add(IntTag(stuckToBlockPos!!.z))
        )
        namedTag!!.putInt(TAG_FAVORED_SLOT, this.favoredSlot)
        namedTag!!.putBoolean(TAG_PLAYER, this.player)
    }

    fun getItem(): Item {
        return if (this.trident != null) trident.clone() else Item.AIR
    }

    fun setItem(item: Item) {
        this.trident = item.clone()
        this.loyaltyLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_LOYALTY)
        this.hasChanneling = trident.hasEnchantment(Enchantment.ID_TRIDENT_CHANNELING)
        this.riptideLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_RIPTIDE)
        this.impalingLevel = trident.getEnchantmentLevel(Enchantment.ID_TRIDENT_IMPALING)
    }

    fun setCritical() {
        this.setCritical(true)
    }

    fun isCritical(): Boolean {
        return this.getDataFlag(EntityFlag.CRITICAL)
    }

    fun setCritical(value: Boolean) {
        this.setDataFlag(EntityFlag.CRITICAL, value)
    }

    override fun getResultDamage(): Int {
        var base: Int = super.getResultDamage()

        if (this.isCritical()) {
            base += ThreadLocalRandom.current().nextInt(base / 2 + 2)
        }

        return base
    }

    override fun getBaseDamage(): Double {
        return 8.0
    }


    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        if (this.isCollided && !this.hadCollision) {
            level!!.addSound(this.position, Sound.ITEM_TRIDENT_HIT_GROUND)
        }

        var hasUpdate: Boolean = super.onUpdate(currentTick)

        if (this.onGround || this.hadCollision) {
            this.setCritical(false)
        }

        if (this.noClip) {
            if (this.canReturnToShooter()) {
                val shooter: Entity? = this.shootingEntity
                val vector3: Vector3 = Vector3(
                    shooter!!.position.x - position.x,
                    shooter.position.y + shooter.getEyeHeight() - position.y,
                    shooter.position.z - position.z
                )
                val bVector: BVector3 = BVector3.fromPos(vector3)
                this.setRotation(bVector.yaw, bVector.pitch)
                this.setMotion(getMotion().multiply(-1.0))
                hasUpdate = true
            } else {
                if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS) && !this.closed) {
                    level!!.dropItem(this.position, this.trident)
                }
                this.close()
            }
        }

        return hasUpdate
    }

    override fun onCollideWithEntity(entity: Entity) {
        if (this.noClip || this.closed) {
            return
        }

        if (this.alreadyCollided) {
            return
        }

        Server.instance.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)))
        var damage: Float = getResultDamage().toFloat()
        if (this.impalingLevel > 0 && (entity.isTouchingWater() || (entity.level!!.isRaining && entity.level!!.canBlockSeeSky(
                entity.position
            )))
        ) {
            damage = damage + (2.5f * impalingLevel.toFloat())
        }
        val ev: EntityDamageEvent = if (this.shootingEntity == null) {
            EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
        } else {
            EntityDamageByChildEntityEvent(this.shootingEntity!!, this, entity, DamageCause.PROJECTILE, damage)
        }
        entity.attack(ev)
        level!!.addSound(this.position, Sound.ITEM_TRIDENT_HIT)
        this.hadCollision = true
        this.alreadyCollided = true
        this.setCollisionPos(this.position)
        this.setMotion(
            Vector3(
                getMotion().x * -0.01,
                getMotion().y * -0.1, getMotion().z * -0.01
            )
        )

        if (this.hasChanneling) {
            if (level!!.isThundering() && level!!.canBlockSeeSky(this.position)) {
                val pos: Locator = this.locator
                val lighting = EntityLightningBolt(pos.chunk, getDefaultNBT(pos.position))
                lighting.spawnToAll()
                level!!.addSound(this.position, Sound.ITEM_TRIDENT_THUNDER)
            }
        }

        if (this.canReturnToShooter()) {
            level!!.addSound(this.position, Sound.ITEM_TRIDENT_RETURN)
            this.setNoClip(true)
            this.hadCollision = false
            this.setTridentRope(true)
        }
    }

    override fun onCollideWithBlock(locator: Locator, motion: Vector3) {
        if (this.noClip) {
            return
        }

        for (collisionBlock: Block in level!!.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) {
            this.setStuckToBlockPos(
                BlockVector3(
                    collisionBlock.position.floorX,
                    collisionBlock.position.floorY,
                    collisionBlock.position.floorZ
                )
            )
            if (this.canReturnToShooter()) {
                level!!.addSound(this.position, Sound.ITEM_TRIDENT_RETURN)
                this.setNoClip(true)
                this.setTridentRope(true)
                return
            }
            onCollideWithBlock(locator, motion, collisionBlock)
        }
    }

    fun getCollisionPos(): Vector3? {
        return collisionPos
    }

    fun setCollisionPos(collisionPos: Vector3?) {
        this.collisionPos = collisionPos
    }

    fun getStuckToBlockPos(): BlockVector3? {
        return stuckToBlockPos
    }

    fun setStuckToBlockPos(stuckToBlockPos: BlockVector3?) {
        this.stuckToBlockPos = stuckToBlockPos
    }

    fun getFavoredSlot(): Int {
        return favoredSlot
    }

    fun setFavoredSlot(favoredSlot: Int) {
        this.favoredSlot = favoredSlot
    }

    override val isCreative: Boolean
        get() = pickupMode == PICKUP_CREATIVE

    fun hasPlayer(): Boolean {
        return player
    }

    fun getLoyaltyLevel(): Int {
        return loyaltyLevel
    }

    fun setLoyaltyLevel(loyaltyLevel: Int) {
        this.loyaltyLevel = loyaltyLevel
        if (loyaltyLevel > 0) {
            trident.addEnchantment(Enchantment.getEnchantment(Enchantment.ID_TRIDENT_LOYALTY).setLevel(loyaltyLevel))
        } else {
            // TODO: this.trident.removeEnchantment(Enchantment.ID_TRIDENT_LOYALTY);
        }
    }

    fun hasChanneling(): Boolean {
        return hasChanneling
    }

    fun setChanneling(hasChanneling: Boolean) {
        this.hasChanneling = hasChanneling
        if (hasChanneling) {
            trident.addEnchantment(Enchantment.getEnchantment(Enchantment.ID_TRIDENT_CHANNELING))
        } else {
            // TODO: this.trident.removeEnchantment(Enchantment.ID_TRIDENT_CHANNELING);
        }
    }

    fun getRiptideLevel(): Int {
        return riptideLevel
    }

    fun setRiptideLevel(riptideLevel: Int) {
        this.riptideLevel = riptideLevel
        if (riptideLevel > 0) {
            trident.addEnchantment(Enchantment.getEnchantment(Enchantment.ID_TRIDENT_RIPTIDE).setLevel(riptideLevel))
        } else {
            // TODO: this.trident.removeEnchantment(Enchantment.ID_TRIDENT_RIPTIDE);
        }
    }

    fun getImpalingLevel(): Int {
        return impalingLevel
    }

    fun setImpalingLevel(impalingLevel: Int) {
        this.impalingLevel = impalingLevel
        if (impalingLevel > 0) {
            trident.addEnchantment(
                Enchantment.getEnchantment(Enchantment.ID_TRIDENT_IMPALING).setLevel(impalingLevel)
            )
        } else {
            // TODO: this.trident.removeEnchantment(Enchantment.ID_TRIDENT_IMPALING);
        }
    }

    fun getTridentRope(): Boolean {
        return this.getDataFlag(EntityFlag.RETURN_TRIDENT)
    }

    fun setTridentRope(tridentRope: Boolean) {
        if (tridentRope) {
            this.setDataProperty(EntityDataTypes.OWNER_EID, shootingEntity!!.getRuntimeID())
        } else {
            this.setDataProperty(EntityDataTypes.OWNER_EID, -1)
        }
        this.setDataFlag(EntityFlag.RETURN_TRIDENT, tridentRope)
    }

    fun canReturnToShooter(): Boolean {
        if (this.loyaltyLevel <= 0) {
            return false
        }

        if (this.getCollisionPos() == defaultCollisionPos && this.getStuckToBlockPos() == defaultStuckToBlockPos) {
            return false
        }

        val shooter: Entity? = this.shootingEntity
        if (shooter != null) {
            if (shooter.isAlive() && shooter is Player) {
                return !(shooter.isSpectator)
            }
        }
        return false
    }

    companion object {
        private const val TAG_PICKUP: String = "pickup"
        private const val TAG_TRIDENT: String = "Trident"
        private const val TAG_FAVORED_SLOT: String = "favoredSlot"
        private const val TAG_CREATIVE: String = "isCreative"
        private const val TAG_PLAYER: String = "player"
        private const val NAME_TRIDENT: String = "Trident"
        private val defaultCollisionPos: Vector3 = Vector3(0.0, 0.0, 0.0)
        private val defaultStuckToBlockPos: BlockVector3 = BlockVector3(0, 0, 0)
    }
}
