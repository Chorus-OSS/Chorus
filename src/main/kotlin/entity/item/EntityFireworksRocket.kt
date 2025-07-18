package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemFireworkRocket
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.protocol.packets.ActorEventPacket
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


open class EntityFireworksRocket(chunk: IChunk?, nbt: CompoundTag) : Entity(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.FIREWORKS_ROCKET
    }

    private val lifetime: Int
    private var fireworkAge: Int = 0
    private var firework: Item
    private var hadCollision: Boolean = false


    init {
        val rand: Random = ThreadLocalRandom.current()
        this.lifetime = 30 + rand.nextInt(12)

        motion.x = rand.nextGaussian() * 0.001
        motion.z = rand.nextGaussian() * 0.001
        motion.y = 0.05

        if (nbt.contains("FireworkItem")) {
            firework = NBTIO.getItemHelper(nbt.getCompound("FireworkItem"))
        } else {
            firework = ItemFireworkRocket()
        }

        if (!firework.hasCompoundTag() || !firework.namedTag!!.contains("Fireworks")) {
            var tag: CompoundTag? = firework.namedTag
            if (tag == null) {
                tag = CompoundTag()
            }

            val ex: CompoundTag = CompoundTag()
                .putByteArray("FireworkColor", byteArrayOf(DyeColor.BLACK.dyeData.toByte()))
                .putByteArray("FireworkFade", byteArrayOf())
                .putBoolean("FireworkFlicker", false)
                .putBoolean("FireworkTrail", false)
                .putByte("FireworkType", ItemFireworkRocket.FireworkExplosion.ExplosionType.CREEPER_SHAPED.ordinal)

            tag.putCompound(
                "Fireworks", CompoundTag()
                    .putList("Explosions", ListTag<CompoundTag>().add(ex))
                    .putByte("Flight", 1)
            )

            firework.setNamedTag(tag)
        }

        //        this.setDataProperty(Entity.HORSE_FLAGS, firework.getNamedTag());//TODO FIXME
        this.setDataProperty(EntityDataTypes.DISPLAY_OFFSET, Vector3f(0f, 1f, 0f))
        this.setDataProperty(EntityDataTypes.CUSTOM_DISPLAY, -1)
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
            motion.x *= 1.15
            motion.z *= 1.15
            motion.y += 0.04
            val locator: Locator = locator
            val motion: Vector3 = getMotion()
            this.move(this.motion.x, this.motion.y, this.motion.z)

            if (this.isCollided && !this.hadCollision) { //collide with block
                this.hadCollision = true

                for (collisionBlock: Block in level!!.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) {
                    collisionBlock.onProjectileHit(this, locator, motion)
                }
            } else if (!this.isCollided && this.hadCollision) {
                this.hadCollision = false
            }

            this.updateMovement()


            val f: Float = sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z).toFloat()
            rotation.yaw = (atan2(this.motion.x, this.motion.z) * (180.0 / Math.PI)).toFloat().toDouble()

            rotation.pitch = (atan2(this.motion.y, f.toDouble()) * (180.0 / Math.PI)).toFloat().toDouble()


            if (this.fireworkAge == 0) {
                level!!.addSound(this.position, Sound.FIREWORK_LAUNCH)
            }

            fireworkAge++

            hasUpdate = true
            if (this.fireworkAge >= this.lifetime) {
                val pk = ActorEventPacket(
                    actorRuntimeID = this.getRuntimeID().toULong(),
                    eventType = ActorEventPacket.Companion.Type.FireworkExplosion,
                    eventData = 0,
                )

                level!!.addLevelSoundEvent(this.position, org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.LargeBlast, -1, getNetworkID())

                Server.broadcastPacket(viewers.values, pk)

                this.kill()
                hasUpdate = true
            }
        }

        return hasUpdate || !this.onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return (source.cause == DamageCause.VOID || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.ENTITY_EXPLOSION || source.cause == DamageCause.BLOCK_EXPLOSION)
                && super.attack(source)
    }

    fun setFirework(item: Item) {
        this.firework = item
        //        this.setDataProperty(Entity.HORSE_FLAGS, item.getNamedTag());//TODO FIXME
    }

    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getOriginalName(): String {
        return "Firework Rocket"
    }
}
