package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFlowingWater
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.vehicle.VehicleMoveEvent
import org.chorus_oss.chorus.event.vehicle.VehicleUpdateEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.AxisAlignedBB.BBConsumer
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.AnimatePacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.ActorLink
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.attribute.AttributeValue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

open class EntityBoat(chunk: IChunk?, nbt: CompoundTag?) : EntityVehicle(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.BOAT
    }

    private val ignoreCollision: MutableSet<Entity> = HashSet(2)
    var woodID: Int = 0
    protected var sinking: Boolean = true
    private var ticksInWater: Int = 0

    init {
        this.setMaxHealth(40)
        this.setHealthSafe(40f)
    }

    override fun initEntity() {
        super.initEntity()
        if (namedTag!!.contains("Variant")) {
            woodID = namedTag!!.getInt("Variant")
        } else if (namedTag!!.contains("woodID")) {
            woodID = namedTag!!.getByte("woodID").toInt()
        }

        this.setDataFlag(EntityFlag.HAS_GRAVITY)
        this.setDataFlag(EntityFlag.STACKABLE)
        entityDataMap.put(EntityDataTypes.VARIANT, woodID)
        entityDataMap.put(EntityDataTypes.IS_BUOYANT, true)
        entityDataMap.put(
            EntityDataTypes.BUOYANCY_DATA,
            "{\"apply_gravity\":true,\"base_buoyancy\":1.0,\"big_wave_probability\":0.02999999932944775,\"big_wave_speed\":10.0,\"drag_down_on_buoyancy_removed\":0.0,\"liquid_blocks\":[\"minecraft:water\",\"minecraft:flowing_water\"],\"simulate_waves\":true}"
        )
        entityDataMap.put(EntityDataTypes.AIR_SUPPLY, 300)
        entityDataMap.put(EntityDataTypes.OWNER_EID, -1)
        entityDataMap.put(EntityDataTypes.ROW_TIME_LEFT, 0)
        entityDataMap.put(EntityDataTypes.ROW_TIME_RIGHT, 0)
        entityDataMap.put(EntityDataTypes.CONTROLLING_RIDER_SEAT_INDEX, 0)
        entityDataMap.put(EntityDataTypes.DATA_LIFETIME_TICKS, -1)
        entityDataMap.put(EntityDataTypes.NAMETAG_ALWAYS_SHOW, -1)
        entityDataMap.put(EntityDataTypes.AMBIENT_SOUND_INTERVAL, 8f)
        entityDataMap.put(EntityDataTypes.AMBIENT_SOUND_INTERVAL_RANGE, 16f)
        entityDataMap.put(EntityDataTypes.AMBIENT_SOUND_EVENT_NAME, "ambient")
        entityDataMap.put(EntityDataTypes.FALL_DAMAGE_MULTIPLIER, 1f)
        entityCollisionReduction = -0.5
    }

    override fun getHeight(): Float {
        return 0.455f
    }

    override fun getWidth(): Float {
        return 1.4f
    }

    override fun getDrag(): Float {
        return 0.1f
    }

    override fun getGravity(): Float {
        return 0.03999999910593033f
    }

    public override fun getBaseOffset(): Float {
        return 0.375f
    }


    override fun getInteractButtonText(player: Player): String {
        return "action.interact.ride.boat"
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (invulnerable) {
            return false
        } else {
            source.damage = (source.damage * 2)

            val attack: Boolean = super.attack(source)

            if (isAlive()) {
                performHurtAnimation()
            }

            return attack
        }
    }

    override fun close() {
        super.close()

        for (linkedEntity: Entity in this.passengers) {
            linkedEntity.riding = null
        }
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

        if (this.isAlive() && passengers.isEmpty()) {
            hasUpdate = this.updateBoat(tickDiff) || hasUpdate
        }

        return hasUpdate || !this.onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    private fun updateBoat(tickDiff: Int): Boolean {
        // The rolling amplitude
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1)
        }

        // A killer task
        if (this.level != null) {
            if (position.y < level!!.minHeight - 16) {
                kill()
                return false
            }
        } else if (position.y < -16) {
            kill()
            return false
        }

        var hasUpdated: Boolean = false
        val waterDiff: Double = getWaterLevel()
        if (!hasControllingPassenger()) {
            hasUpdated = computeBuoyancy(waterDiff)
            val iterator: MutableIterator<Entity> = ignoreCollision.iterator()
            while (iterator.hasNext()) {
                val ignored: Entity = iterator.next()
                if (ignored.isClosed() || !ignored.isAlive() || !ignored.getBoundingBox()
                        .intersectsWith(getBoundingBox().grow(0.5, 0.5, 0.5))
                ) {
                    iterator.remove()
                    hasUpdated = true
                }
            }
            moveBoat(waterDiff)
        } else {
            updateMovement()
        }
        hasUpdated = hasUpdated || positionChanged
        if (waterDiff >= -SINKING_DEPTH) {
            if (ticksInWater != 0) {
                ticksInWater = 0
                hasUpdated = true
            }
            //hasUpdated = collectCollidingEntities() || hasUpdated;
        } else {
            hasUpdated = true
            ticksInWater += tickDiff
            if (ticksInWater >= 3 * 20) {
                for (i in passengers.indices.reversed()) {
                    dismountEntity(passengers[i])
                }
            }
        }
        Server.instance.pluginManager.callEvent(VehicleUpdateEvent(this))
        return hasUpdated
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return super.canCollideWith(entity) && !isPassenger(entity)
    }

    override fun canDoInteraction(): Boolean {
        return passengers.size < 2
    }

    private fun moveBoat(waterDiff: Double) {
        checkObstruction(position.x, position.y, position.z)
        move(motion.x, motion.y, motion.z)

        var friction: Double = (1 - this.getDrag()).toDouble()

        if (this.onGround && (abs(motion.x) > 0.00001 || abs(
                motion.z
            ) > 0.00001)
        ) {
            friction *= level!!.getBlock(position.add(0.0, -1.0, 0.0).floor()).frictionFactor
        }

        motion.x *= friction

        if (!onGround || waterDiff > SINKING_DEPTH /* || sinking*/) {
            motion.y =
                if (waterDiff > 0.5) motion.y - this.getGravity() else (if (motion.y - SINKING_SPEED < -SINKING_MAX_SPEED) motion.y else motion.y - SINKING_SPEED)
        }

        motion.z *= friction

        val from: Transform = Transform(
            prevPosition.x,
            prevPosition.y, prevPosition.z, prevRotation.yaw, prevRotation.pitch,
            level!!
        )
        val to: Transform = Transform(
            position.x,
            position.y, position.z, rotation.yaw, rotation.pitch,
            level!!
        )

        if (from != to) {
            Server.instance.pluginManager.callEvent(VehicleMoveEvent(this, from, to))
        }

        // TODO: lily pad collision
        this.updateMovement()
    }

    private fun collectCollidingEntities(): Boolean {
        if (passengers.size >= 2) {
            return false
        }

        for (entity: Entity in level!!.getCollidingEntities(
            boundingBox.grow(0.20000000298023224, 0.0, 0.20000000298023224),
            this
        )) {
            if (entity.riding != null || (entity !is EntityLiving) || entity is Player || entity is EntitySwimmable || isPassenger(
                    entity
                )
            ) {
                continue
            }

            this.mountEntity(entity)

            if (passengers.size >= 2) {
                break
            }
        }

        return true
    }

    private fun computeBuoyancy(waterDiff: Double): Boolean {
        var waterDiff: Double = waterDiff
        var hasUpdated: Boolean = false
        waterDiff -= (getBaseOffset() / 4).toDouble()
        if (waterDiff > SINKING_DEPTH && !sinking) {
            sinking = true
        } else if (waterDiff < -SINKING_DEPTH && sinking) {
            sinking = false
        }

        if (waterDiff < -SINKING_DEPTH / 1.7) {
            motion.y = min(0.05 / 10, motion.y + 0.005)
            hasUpdated = true
        } else if (waterDiff < 0 || !sinking) {
            motion.y = if (motion.y > (SINKING_MAX_SPEED / 2)) max(
                motion.y - 0.02, (SINKING_MAX_SPEED / 2)
            ) else motion.y + SINKING_SPEED
            hasUpdated = true
        }
        return hasUpdated
    }

    override fun updatePassengers() {
        updatePassengers(false)
    }

    fun updatePassengers(sendLinks: Boolean) {
        if (passengers.isEmpty()) {
            return
        }

        for (passenger: Entity in ArrayList(passengers)) {
            if (!passenger.isAlive()) {
                dismountEntity(passenger)
            }
        }

        var ent: Entity

        if (passengers.size == 1) {
            (passengers.get(0).also { ent = it }).setSeatPosition(getMountedOffset(ent))
            super.updatePassengerPosition(ent)

            if (sendLinks) {
                broadcastLinkPacket(ent, ActorLink.Companion.Type.Rider)
            }
        } else if (passengers.size == 2) {
            if ((passengers.get(0).also { ent = it }) !is Player) { //swap
                val passenger2: Entity = passengers.get(1)

                if (passenger2 is Player) {
                    passengers.set(0, passenger2)
                    passengers.set(1, ent)

                    ent = passenger2
                }
            }

            ent.setSeatPosition(getMountedOffset(ent).add(RIDER_PASSENGER_OFFSET))
            super.updatePassengerPosition(ent)
            if (sendLinks) {
                broadcastLinkPacket(ent, ActorLink.Companion.Type.Rider)
            }

            (passengers.get(1).also { ent = it }).setSeatPosition(getMountedOffset(ent).add(PASSENGER_OFFSET))

            super.updatePassengerPosition(ent)

            if (sendLinks) {
                broadcastLinkPacket(ent, ActorLink.Companion.Type.Passenger)
            }

            //float yawDiff = ent.getId() % 2 == 0 ? 90 : 270;
            //ent.setRotation(this.yaw + yawDiff, ent.pitch);
            //ent.updateMovement();
        } else {
            for (passenger: Entity in passengers) {
                super.updatePassengerPosition(passenger)
            }
        }
    }

    fun getWaterLevel(): Double {
        val maxY: Double = boundingBox.minY + getBaseOffset()
        val consumer = object : BBConsumer<Double> {
            private var diffY: Double = Double.MAX_VALUE

            override fun accept(x: Int, y: Int, z: Int) {
                var block: Block =
                    level!!.getBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))

                if (block is BlockFlowingWater || ((block.getLevelBlockAtLayer(1)
                        .also { block = it }) is BlockFlowingWater)
                ) {
                    val level: Double = block.maxY

                    diffY = min(maxY - level, diffY)
                }
            }

            override fun get(): Double {
                return diffY
            }
        }

        boundingBox.forEach(consumer)

        return consumer.get()
    }

    override fun mountEntity(entity: Entity): Boolean {
        val player: Boolean = !passengers.isEmpty() && passengers.get(0) is Player
        var mode = ActorLink.Companion.Type.Passenger

        if (!player && (entity is Player || passengers.isEmpty())) {
            mode = ActorLink.Companion.Type.Rider
        }

        return super.mountEntity(entity, mode)
    }

    override fun mountEntity(entity: Entity, mode: ActorLink.Companion.Type): Boolean {
        val r: Boolean = super.mountEntity(entity, mode)
        if (entity.riding === this) {
            updatePassengers(true)

            entity.setDataProperty(EntityDataTypes.SEAT_LOCK_RIDER_ROTATION, true)
            entity.setDataProperty(EntityDataTypes.SEAT_LOCK_RIDER_ROTATION_DEGREES, 90)
            entity.setDataProperty(EntityDataTypes.SEAT_HAS_ROTATION, passengers.indexOf(entity) != 1)
            entity.setDataProperty(EntityDataTypes.SEAT_ROTATION_OFFSET_DEGREES, -90)
            entity.setRotation(rotation.yaw, entity.rotation.pitch)
            entity.updateMovement()
        }
        return r
    }

    override fun updatePassengerPosition(passenger: Entity) {
        updatePassengers()
    }

    override fun dismountEntity(entity: Entity, sendLinks: Boolean): Boolean {
        val r: Boolean = super.dismountEntity(entity, sendLinks)

        updatePassengers()
        entity.setDataProperty(EntityDataTypes.SEAT_LOCK_RIDER_ROTATION, false)
        if (entity is EntityHuman) {
            ignoreCollision.add(entity)
        }

        return r
    }

    override fun isControlling(entity: Entity?): Boolean {
        return entity is Player && passengers.indexOf(entity) == 0
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (passengers.size >= 2 || getWaterLevel() < -SINKING_DEPTH) {
            return false
        }

        super.mountEntity(player)
        return super.onInteract(player, item, clickedPos)
    }

    override fun getMountedOffset(entity: Entity?): Vector3f {
        return if (entity is Player) RIDER_PLAYER_OFFSET else RIDER_OFFSET
    }

    fun onPaddle(animation: AnimatePacket.Action, value: Float) {
        val propertyId =
            if (animation == AnimatePacket.Action.ROW_RIGHT) EntityDataTypes.ROW_TIME_RIGHT else EntityDataTypes.ROW_TIME_LEFT

        if (getDataProperty(propertyId).compareTo(value) != 0) {
            this.setDataProperty(propertyId, value)
        }
    }

    override fun applyEntityCollision(entity: Entity) {
        if (this.riding == null && !hasControllingPassenger() && entity.riding !== this && !entity.passengers.contains(
                this
            ) && !ignoreCollision.contains(entity)
        ) {
            if (!entity.boundingBox.intersectsWith(boundingBox.grow(0.20000000298023224, -0.1, 0.20000000298023224))
                || entity is Player && entity.isSpectator
            ) {
                return
            }

            var diffX: Double = entity.position.x - position.x
            var diffZ: Double = entity.position.z - position.z

            var direction: Double = max(abs(diffX), abs(diffZ))

            if (direction >= 0.009999999776482582) {
                direction = sqrt(direction)
                diffX /= direction
                diffZ /= direction

                val d3: Double = min(1 / direction, 1.0)

                diffX *= d3
                diffZ *= d3
                diffX *= 0.05000000074505806
                diffZ *= 0.05000000074505806
                diffX *= 1 + entityCollisionReduction
                diffZ *= 1 + entityCollisionReduction

                if (this.riding == null) {
                    motion.x -= diffX
                    motion.z -= diffZ
                }
            }
        }
    }

    override fun canPassThrough(): Boolean {
        return false
    }


    override fun kill() {
        if (!isAlive()) {
            return
        }
        super.kill()

        if (lastDamageCause is EntityDamageByEntityEvent) {
            val damageByEntityCause = lastDamageCause as EntityDamageByEntityEvent
            val damager: Entity = damageByEntityCause.damager
            if (damager is Player && damager.isCreative) {
                return
            }
        }

        if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
            dropItem()
        }
    }

    protected open fun dropItem() {
        level!!.dropItem(this.position, Item.get(ItemID.BOAT, this.woodID))
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putInt("Variant", this.woodID) // Correct way in Bedrock Edition
        namedTag!!.putByte("woodID", this.woodID) // Compatibility with Cloudburst Nukkit
    }

    fun getBoatVariant(): Int {
        return this.woodID
    }

    fun setBoatVariant(variant: Int) {
        this.woodID = variant
        entityDataMap[EntityDataTypes.VARIANT] = variant
    }

    override fun getOriginalName(): String {
        return "Boat"
    }

    fun onInput(loc: Transform) {
        this.move(loc.position.x - position.x, loc.position.y - position.y, loc.position.z - position.z)
        rotation.yaw = loc.rotation.yaw
        broadcastMovement(false)
    }

    companion object {
        val RIDER_PLAYER_OFFSET: Vector3f = Vector3f(0f, 1.02001f, 0f)
        val RIDER_OFFSET: Vector3f = Vector3f(0f, -0.2f, 0f)

        val PASSENGER_OFFSET: Vector3f = Vector3f(-0.6f)
        val RIDER_PASSENGER_OFFSET: Vector3f = Vector3f(0.2f)

        const val RIDER_INDEX: Int = 0
        const val PASSENGER_INDEX: Int = 1

        const val SINKING_DEPTH: Double = 0.07
        const val SINKING_SPEED: Double = 0.0005
        const val SINKING_MAX_SPEED: Double = 0.005
    }
}
