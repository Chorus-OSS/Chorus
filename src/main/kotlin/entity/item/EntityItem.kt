package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.ItemDespawnEvent
import org.chorus_oss.chorus.event.entity.ItemSpawnEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.item.ItemStack
import kotlin.math.abs

class EntityItem(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.ITEM
    }

    var owner: String? = null
    protected var thrower: String? = null
    lateinit var item: Item
    var pickupDelay: Int = 0

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

    public override fun getDrag(): Float {
        return 0.02f
    }

    override fun getBaseOffset(): Float {
        return 0.125f
    }

    override fun canCollide(): Boolean {
        return false
    }

    override fun initEntity() {
        super.initEntity()

        this.setMaxHealth(5)
        this.setHealthSafe(namedTag!!.getShort("Health").toFloat())

        if (namedTag!!.contains("Age")) {
            this.age = namedTag!!.getShort("Age").toInt()
        }

        if (namedTag!!.contains("PickupDelay")) {
            this.pickupDelay = namedTag!!.getShort("PickupDelay").toInt()
        }

        if (namedTag!!.contains("Owner")) {
            this.owner = namedTag!!.getString("Owner")
        }

        if (namedTag!!.contains("Thrower")) {
            this.thrower = namedTag!!.getString("Thrower")
        }

        if (!namedTag!!.contains("Item")) {
            this.close()
            return
        }

        this.item = NBTIO.getItemHelper(namedTag!!.getCompound("Item"))
        this.setDataFlag(EntityFlag.HAS_GRAVITY, true)

        if (item.isLavaResistant) {
            this.fireProof = true // Netherite items are fireproof
        }

        Server.instance.pluginManager.callEvent(ItemSpawnEvent(this))
    }


    override fun attack(source: EntityDamageEvent): Boolean {
        if (item != null && item.isLavaResistant && (source.cause == DamageCause.LAVA || source.cause == DamageCause.FIRE || source.cause == DamageCause.FIRE_TICK)) {
            return false
        }

        return (source.cause == DamageCause.VOID || source.cause == DamageCause.CONTACT || source.cause == DamageCause.FIRE_TICK || (source.cause == DamageCause.ENTITY_EXPLOSION ||
                source.cause == DamageCause.BLOCK_EXPLOSION) && !this.isInsideOfWater() && (this.item == null ||
                item.id !== ItemID.NETHER_STAR)) && super.attack(source)
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

        if (this.age % 60 == 0 && this.onGround && this.item != null && this.isAlive()) {
            if (item.getCount() < item.maxStackSize) {
                for (entity: Entity in level!!.getNearbyEntities(
                    getBoundingBox().grow(1.0, 1.0, 1.0),
                    this, false
                )) {
                    if (entity is EntityItem) {
                        if (!entity.isAlive()) {
                            continue
                        }
                        val closeItem: Item = entity.item
                        if (!closeItem.equals(item, true, true)) {
                            continue
                        }
                        if (!entity.isOnGround()) {
                            continue
                        }
                        val newAmount: Int = item.getCount() + closeItem.getCount()
                        if (newAmount > item.maxStackSize) {
                            continue
                        }
                        entity.close()
                        item.setCount(newAmount)
                        val packet: EntityEventPacket = EntityEventPacket()
                        packet.eid = getRuntimeID()
                        packet.data = newAmount
                        packet.event = EntityEventPacket.MERGE_ITEMS
                        Server.broadcastPacket(viewers.values, packet)
                    }
                }
            }
        }

        var hasUpdate: Boolean = this.entityBaseTick(tickDiff)

        val lavaResistant: Boolean = fireProof || item != null && item.isLavaResistant

        if (!lavaResistant && (isInsideOfFire() || isInsideOfLava())) {
            this.kill()
        }

        if (this.isAlive()) {
            if (this.pickupDelay > 0 && this.pickupDelay < 32767) {
                this.pickupDelay -= tickDiff
                if (this.pickupDelay < 0) {
                    this.pickupDelay = 0
                }
            } /* else { // Done in Player#checkNearEntities
                for (Entity entity : this.level.getNearbyEntities(this.boundingBox.grow(1, 0.5, 1), this)) {
                    if (entity instanceof Player) {
                        if (((Player) entity).pickupEntity(this, true)) {
                            return true;
                        }
                    }
                }
            }*/

            var bid: String = level!!.getBlockIdAt(
                position.x.toInt(),
                boundingBox.maxY.toInt(), position.z.toInt(), 0
            )
            if (bid === BlockID.FLOWING_WATER || bid === BlockID.WATER || (level!!.getBlockIdAt(
                    position.x.toInt(),
                    boundingBox.maxY.toInt(), position.z.toInt(), 1
                ).also { bid = it }) === BlockID.FLOWING_WATER || bid === BlockID.WATER
            ) {
                //item is fully in water or in still water
                motion.y -= this.getGravity() * -0.015
            } else if (lavaResistant && (level!!.getBlockIdAt(
                    position.x.toInt(),
                    boundingBox.maxY.toInt(), position.z.toInt(), 0
                ) === BlockID.FLOWING_LAVA || level!!.getBlockIdAt(
                    position.x.toInt(),
                    boundingBox.maxY.toInt(), position.z.toInt(), 0
                ) === BlockID.LAVA || level!!.getBlockIdAt(
                    position.x.toInt(),
                    boundingBox.maxY.toInt(), position.z.toInt(), 1
                ) === BlockID.FLOWING_LAVA || level!!.getBlockIdAt(
                    position.x.toInt(),
                    boundingBox.maxY.toInt(), position.z.toInt(), 1
                ) === BlockID.LAVA
                        )
            ) {
                //item is fully in lava or in still lava
                motion.y -= this.getGravity() * -0.015
            } else if (this.isInsideOfWater() || lavaResistant && this.isInsideOfLava()) {
                motion.y = this.getGravity() - 0.06 //item is going up in water, don't let it go back down too fast
            } else {
                motion.y -= getGravity().toDouble() //item is not in water
            }

            if (this.checkObstruction(
                    position.x,
                    position.y, position.z
                )
            ) {
                hasUpdate = true
            }

            this.move(motion.x, motion.y, motion.z)

            var friction: Double = (1 - this.getDrag()).toDouble()

            if (this.onGround && (abs(motion.x) > 0.00001 || abs(
                    motion.z
                ) > 0.00001)
            ) {
                friction *= level!!.getBlock(position.add(0.0, -1.0, 0.0).floor()).frictionFactor
            }

            motion.x *= friction
            motion.y *= (1 - this.getDrag()).toDouble()
            motion.z *= friction

            if (this.onGround) {
                motion.y *= -0.5
            }

            this.updateMovement()

            if (this.age > 6000) {
                val ev: ItemDespawnEvent = ItemDespawnEvent(this)
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    this.age = 0
                } else {
                    this.kill()
                    hasUpdate = true
                }
            }
        }

        return hasUpdate || !this.onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    override fun setOnFire(seconds: Int) {
        if (item != null && item.isLavaResistant) {
            return
        }
        super.setOnFire(seconds)
    }

    override fun saveNBT() {
        super.saveNBT()
        if (this.item != null) { // Yes, a item can be null... I don't know what causes this, but it can happen.
            namedTag!!.putCompound("Item", NBTIO.putItemHelper(this.item, -1))
            namedTag!!.putShort("Health", health.toInt())
            namedTag!!.putShort("Age", this.age)
            namedTag!!.putShort("PickupDelay", this.pickupDelay)
            if (this.owner != null) {
                namedTag!!.putString("Owner", owner!!)
            }

            if (this.thrower != null) {
                namedTag!!.putString("Thrower", thrower!!)
            }
        }
    }

    override fun getOriginalName(): String {
        return "Item"
    }

    override fun getEntityName(): String {
        if (this.hasCustomName()) {
            return getNameTag()
        }
        return item.count.toString() + "x " + item.displayName
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return false
    }

    public override fun createAddEntityPacket(): Packet {
        return org.chorus_oss.protocol.packets.AddItemActorPacket(
            actorUniqueID = this.uniqueId,
            actorRuntimeID = this.runtimeId.toULong(),
            position = Vector3f(this.position),
            velocity = Vector3f(this.motion),
            item = ItemStack(this.item),
            actorData = ActorDataMap(this.entityDataMap),
            fromFishing = false
        )
    }

    override fun doesTriggerPressurePlate(): Boolean {
        return true
    }
}
