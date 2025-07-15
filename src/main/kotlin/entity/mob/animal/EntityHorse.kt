package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockDirt
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockTurtleEgg
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.ai.EntityAI
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.PassByTimeEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.ProbabilityEvaluator
import org.chorus_oss.chorus.entity.ai.executor.AnimalGrowExecutor
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.LookAtTargetExecutor
import org.chorus_oss.chorus.entity.ai.executor.TameHorseExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestFeedingPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.block.FarmLandDecayEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityFallEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.HorseInventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.network.protocol.UpdateAttributesPacket
import org.chorus_oss.chorus.utils.Utils
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.ActorLink
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.attribute.AttributeValue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil

open class EntityHorse(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable, EntityVariant,
    EntityMarkVariant, EntityRideable, EntityOwnable, InventoryHolder {
    override fun getEntityIdentifier(): String {
        return EntityID.HORSE
    }

    override var variant: Int
        get() = super<EntityVariant>.variant
        set(value) {
            super<EntityVariant>.variant = value
        }

    private lateinit var attributeMap: MutableMap<String, Attribute>
    override lateinit var inventory: HorseInventory
    private val jumping = AtomicBoolean(false)

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.7f
        }
        return 1.4f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.8f
        }
        return 1.6f
    }

    public override fun initEntity() {
        attributeMap = HashMap()
        if (namedTag!!.containsList("Attributes")) {
            for (nbt in namedTag!!.getList<CompoundTag>("Attributes", CompoundTag::class.java).all) {
                attributeMap[nbt.getString("Name")] = Attribute.Companion.fromNBT(nbt)
            }
        } else {
            for (attribute in randomizeAttributes()) {
                attributeMap[attribute.name] = attribute
            }
        }
        this.maxHealth =
            ceil(attributeMap["minecraft:health"]!!.maxValue.toDouble()).toInt()
        super.initEntity()

        this.inventory = HorseInventory(this)
        val inventoryTag: ListTag<CompoundTag>
        if (namedTag!!.containsList("Inventory")) {
            inventoryTag = namedTag!!.getList("Inventory", CompoundTag::class.java)
            val item0 = NBTIO.getItemHelper(inventoryTag[0])
            if (item0.isNothing) {
                this.setDataFlag(EntityFlag.SADDLED, false)
                this.setDataFlag(EntityFlag.WASD_CONTROLLED, false)
                this.setDataFlag(EntityFlag.CAN_POWER_JUMP, false)
            } else {
                this.inventory.setItem(0, item0)
            }
            this.inventory.setItem(1, NBTIO.getItemHelper(inventoryTag[1]))
        } else {
            this.setDataFlag(EntityFlag.SADDLED, false)
            this.setDataFlag(EntityFlag.WASD_CONTROLLED, false)
            this.setDataFlag(EntityFlag.CAN_POWER_JUMP, false)
        }
        this.setDataFlag(EntityFlag.HAS_GRAVITY, true)
        this.setDataFlag(EntityFlag.CAN_CLIMB, true)
        this.setDataFlag(EntityFlag.HAS_COLLISION, true)

        if (!hasVariant()) {
            this.variant = (randomVariant())
        }
        if (!hasMarkVariant()) {
            this.setMarkVariant(randomMarkVariant())
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        val inventoryTag = ListTag<CompoundTag>()
        if (this.inventory != null) {
            val item0 = this.inventory.getItem(0)
            val item1 = this.inventory.getItem(1)
            inventoryTag.add(NBTIO.putItemHelper(item0, 0))
            inventoryTag.add(NBTIO.putItemHelper(item1, 1))
        }
        namedTag!!.putList("Inventory", inventoryTag)

        val compoundTagListTag = ListTag<CompoundTag>()
        for (attribute in attributeMap.values) {
            compoundTagListTag.add(Attribute.Companion.toNBT(attribute))
        }
        namedTag!!.putList("Attributes", compoundTagListTag)
    }

    override fun setHealthSafe(health: Float) {
        super.setHealthSafe(health)
        if (this.isAlive()) {
            val attr = attributeMap["minecraft:health"]!!
                .setDefaultValue(maxHealth.toFloat())
                .setMaxValue(maxHealth.toFloat())
                .setValue(if (health > 0) (if (health < maxHealth) health else maxHealth.toFloat()) else 0f)
            val pk = UpdateAttributesPacket()
            pk.entries = arrayOf(attr)
            pk.entityId = this.getRuntimeID()
            Server.broadcastPacket(this.viewers.values.toTypedArray(), pk)
        }
    }

    override fun setMaxHealth(maxHealth: Int) {
        super.setMaxHealth(maxHealth)
        val attr = attributeMap["minecraft:health"]!!
            .setMaxValue(maxHealth.toFloat())
            .setDefaultValue(maxHealth.toFloat())
            .setValue(if (health > 0) (if (health < getMaxHealth()) health else getMaxHealth().toFloat()) else 0f)
        if (this.isAlive()) {
            val pk = UpdateAttributesPacket()
            pk.entries = arrayOf(attr)
            pk.entityId = this.getRuntimeID()
            Server.broadcastPacket(this.viewers.values.toTypedArray(), pk)
        }
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(Item.get(ItemID.LEATHER), getHorseArmor(), getSaddle())
    }

    override fun getOriginalName(): String {
        return "Horse"
    }

    override fun getAllVariant(): IntArray {
        return VARIANTS
    }

    override fun getAllMarkVariant(): IntArray {
        return MARK_VARIANTS
    }

    fun getJumping(): AtomicBoolean {
        return jumping
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>( //todo breed
                Behavior(
                    AnimalGrowExecutor(),  //todo：Growth rate
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.ENTITY_SPAWN_TIME, 20 * 60 * 20, Int.MAX_VALUE),
                        IBehaviorEvaluator { entity: EntityMob -> entity is EntityAnimal && entity.isBaby() }
                    ),
                    1, 1, 1200
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    TameHorseExecutor(0.4f, 12, 40, true, 100, true, 10, 35), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.RIDER_NAME),
                        IBehaviorEvaluator { e: EntityMob? -> !this.hasOwner(false) }
                    ), 4, 1),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ProbabilityEvaluator(4, 10),
                    1,
                    1,
                    100
                ),
                Behavior(
                    FlatRandomRoamExecutor(0.2f, 12, 100, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(NearestFeedingPlayerSensor(8.0, 0.0), NearestPlayerSensor(8.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun asyncPrepare(currentTick: Int) {
        if (this.getRider() == null || this.owner == null || getSaddle().isNothing) {
            isActive = level!!.isHighLightChunk(getChunkX(), getChunkZ())
            if (!this.isImmobile()) {
                behaviorGroup.collectSensorData(this)
                behaviorGroup.evaluateCoreBehaviors(this)
                behaviorGroup.evaluateBehaviors(this)
                behaviorGroup.tickRunningCoreBehaviors(this)
                behaviorGroup.tickRunningBehaviors(this)
                behaviorGroup.updateRoute(this)
                behaviorGroup.applyController(this)
                if (EntityAI.checkDebugOption(EntityAI.DebugOption.BEHAVIOR)) behaviorGroup.debugTick(
                    this
                )
            }
            this.needsReCalcMovement =
                level!!.tickRateOptDelay == 1 || ((currentTick + tickSpread) and (level!!.tickRateOptDelay - 1)) == 0
            this.calculateOffsetBoundingBox()
            if (!this.isImmobile()) {
                handleGravity()
                if (needsReCalcMovement) {
                    handleCollideMovement(currentTick)
                }
                addTmpMoveMotionXZ(previousCollideMotion)
                handleFloatingMovement()
                handleGroundFrictionMovement()
                handlePassableBlockFrictionMovement()
            }
        }
    }

    override fun fall(fallDistance: Float) {
        var fallDistance = fallDistance
        if (this.hasEffect(EffectType.SLOW_FALLING)) {
            return
        }

        val floorLocation = position.floor()
        val down = level!!.getBlock(floorLocation.down())

        val event = EntityFallEvent(this, down, fallDistance)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return
        }
        fallDistance = event.fallDistance

        if ((!this.isPlayer || level!!.gameRules
                .getBoolean(GameRule.FALL_DAMAGE)) && down.useDefaultFallDamage()
        ) {
            val jumpBoost =
                if (this.hasEffect(EffectType.JUMP_BOOST)) getEffect(EffectType.JUMP_BOOST)!!.getLevel() else 0
            val damage = fallDistance - 3 - jumpBoost - getClientMaxJumpHeight()

            if (damage > 0) {
                this.attack(EntityDamageEvent(this, DamageCause.FALL, damage))
            }
        }

        down.onEntityFallOn(this, fallDistance)

        if (fallDistance > 0.75) { // TODO: moving these into their own classes (method "onEntityFallOn()")
            if (down.id == BlockID.FARMLAND) {
                if (onPhysicalInteraction(down, false)) {
                    return
                }
                val farmEvent = FarmLandDecayEvent(this, down)
                Server.instance.pluginManager.callEvent(farmEvent)
                if (farmEvent.cancelled) return
                level!!.setBlock(down.position, BlockDirt(), false, true)
                return
            }

            val floor = level!!.getTickCachedBlock(floorLocation)
            if (floor is BlockTurtleEgg) {
                if (onPhysicalInteraction(floor, ThreadLocalRandom.current().nextInt(10) >= 3)) {
                    return
                }
                level!!.useBreakOn(this.position, null, null, true)
            }
        }
    }

    override fun onUpdate(currentTick: Int): Boolean {
        val b = super.onUpdate(currentTick)
        if (currentTick % 2 == 0) {
            if (this.jumping != null && jumping.get() && this.isOnGround()) {
                this.setDataFlag(EntityFlag.STANDING, false)
                jumping.set(false)
            }
        }
        return b
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return super.canCollideWith(entity) && entity !== this.getRider()
    }

    fun onInput(clientLoc: Transform) {
        if (this.getRider() == null || this.owner == null || getSaddle().isNothing) return
        //每次输入乘骑玩家位置之前都要确保motion为0,避免onGround不更新
        motion.x = 0.0
        motion.y = 0.0
        motion.z = 0.0
        this.moveTarget = null
        this.lookTarget = null
        this.move(
            clientLoc.position.x - position.x,
            clientLoc.position.y - position.y,
            clientLoc.position.z - position.z
        )
        rotation.yaw = clientLoc.rotation.yaw
        this.headYaw = clientLoc.headYaw
        broadcastMovement(false)
    }

    override fun getOwnerName(): String? {
        val ownerName = super.getOwnerName()
        if (ownerName == null) {
            this.setDataProperty(EntityDataTypes.CONTAINER_TYPE, 0)
            this.setDataProperty(EntityDataTypes.CONTAINER_SIZE, 0)
        } else {
            //添加两个metadata这个才能交互物品栏
            this.setDataProperty(EntityDataTypes.CONTAINER_TYPE, 12)
            this.setDataProperty(EntityDataTypes.CONTAINER_SIZE, 2)
        }
        return ownerName
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        mountEntity(player)
        return false
    }

    override fun mountEntity(entity: Entity): Boolean {
        this.memoryStorage[CoreMemoryTypes.RIDER_NAME] = entity.getEntityName()
        super.mountEntity(entity, ActorLink.Companion.Type.Rider)
        return true
    }

    override fun dismountEntity(entity: Entity): Boolean {
        this.memoryStorage.clear(CoreMemoryTypes.RIDER_NAME)
        return super.dismountEntity(entity)
    }

    override fun getMountedOffset(entity: Entity?): Vector3f {
        return Vector3f(0f, 2.42f, 0f)
    }

    fun getRider(): Entity? {
        val name = memoryStorage.get<String>(CoreMemoryTypes.RIDER_NAME)
        return if (name != null) {
            Server.instance.getPlayerExact(name)
        } else null //todo other entity
    }

    fun getClientMaxJumpHeight(): Float {
        return attributeMap["minecraft:horse.jump_strength"]!!.getValue()
    }

    /**
     * @see HorseInventory.setSaddle
     */
    fun setSaddle(item: Item) {
        this.inventory.saddle = item
    }

    /**
     * @see HorseInventory.setHorseArmor
     */
    fun setHorseArmor(item: Item) {
        this.inventory.horseArmor = item
    }

    /**
     * @see HorseInventory.getSaddle
     */
    fun getSaddle(): Item {
        return this.inventory.saddle
    }

    /**
     * @see HorseInventory.getHorseArmor
     */
    fun getHorseArmor(): Item {
        return this.inventory.horseArmor
    }

    /**
     * 播放驯服失败的动画
     *
     *
     * Play an animation of a failed tamer
     */
    fun playTameFailAnimation() {
        level!!.addLevelSoundEvent(
            this.position, LevelSoundEventPacket.SOUND_MAD, -1, "minecraft:horse",
            this.isBaby(), false
        )
        this.setDataFlag(EntityFlag.STANDING)
    }

    /**
     * 停止播放驯服失败的动画
     *
     *
     * Stop playing the animation that failed to tame
     */
    fun stopTameFailAnimation() {
        this.setDataFlag(EntityFlag.STANDING, false)
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        val attr = attributeMap["minecraft:health"]!!
            .setDefaultValue(maxHealth.toFloat())
            .setMaxValue(maxHealth.toFloat())
            .setValue(if (health > 0) (if (health < maxHealth) health else maxHealth.toFloat()) else 0f)
        val pk = UpdateAttributesPacket()
        pk.entries = arrayOf(attr)
        pk.entityId = this.getRuntimeID()
        player.dataPacket(pk)
    }

    protected fun generateRandomMaxHealth(): Float {
        return 15.0f + Utils.rand(0, 8).toFloat() + Utils.rand(0, 9).toFloat()
    }

    protected fun generateRandomJumpStrength(): Float {
        return (0.4f + Utils.random.nextDouble() * 0.2 + Utils.random.nextDouble() * 0.2 + Utils.random.nextDouble() * 0.2).toFloat()
    }

    protected fun generateRandomSpeed(): Float {
        return ((0.45f + Utils.random.nextDouble() * 0.3 + Utils.random.nextDouble() * 0.3 + Utils.random.nextDouble() * 0.3) * 0.25).toFloat()
    }

    protected fun randomizeAttributes(): Array<Attribute> {
        val maxHealth = generateRandomMaxHealth()
        val attributes = arrayOf(
            Attribute.Companion.getAttribute(Attribute.Companion.MOVEMENT_SPEED)
                .setValue(generateRandomSpeed()),
            Attribute.Companion.getAttribute(Attribute.Companion.MAX_HEALTH)
                .setMinValue(0f).setMaxValue(maxHealth).setDefaultValue(maxHealth).setValue(maxHealth),
            Attribute.Companion.getAttribute(Attribute.Companion.HORSE_JUMP_STRENGTH)
                .setValue(generateRandomJumpStrength())
        )
        val compoundTagListTag = ListTag<CompoundTag>()
        compoundTagListTag.add(Attribute.Companion.toNBT(attributes[0])).add(
            Attribute.Companion.toNBT(
                attributes[1]
            )
        ).add(Attribute.Companion.toNBT(attributes[2]))
        namedTag!!.putList("Attributes", compoundTagListTag)
        return attributes
    }

    override fun createAddEntityPacket(): Packet {
        return org.chorus_oss.protocol.packets.AddActorPacket(
            actorUniqueID = this.uniqueId,
            actorRuntimeID = this.runtimeId.toULong(),
            actorType = this.getEntityIdentifier(),
            position = org.chorus_oss.protocol.types.Vector3f(
                this.position.add(
                    0.0,
                    this.getBaseOffset().toDouble(),
                    0.0
                )
            ),
            velocity = org.chorus_oss.protocol.types.Vector3f(this.motion),
            rotation = org.chorus_oss.protocol.types.Vector2f(this.rotation),
            headYaw = this.rotation.yaw.toFloat(),
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

    override fun isBreedingItem(item: Item): Boolean {
        return item.id == ItemID.GOLDEN_APPLE || item.id == ItemID.GOLDEN_CARROT
    }

    companion object {
        private val VARIANTS = intArrayOf(0, 1, 2, 3, 4, 5, 6)
        private val MARK_VARIANTS = intArrayOf(0, 1, 2, 3, 4)
    }
}
