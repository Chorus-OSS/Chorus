package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityExplosive
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityExplosionPrimeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTntMinecart
import org.chorus_oss.chorus.level.Explosion
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.MinecartType
import org.chorus_oss.protocol.types.ActorLink
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.sqrt

class EntityTntMinecart(chunk: IChunk?, nbt: CompoundTag) : EntityMinecartAbstract(chunk, nbt), EntityExplosive {
    override fun getEntityIdentifier(): String {
        return EntityID.TNT_MINECART
    }

    private var fuse: Int = 0

    init {
        super.setDisplayBlock(Block.get(BlockID.TNT), false)
    }

    override fun isRideable(): Boolean {
        return false
    }

    override fun initEntity() {
        super.initEntity()

        if (namedTag!!.contains("TNTFuse")) {
            fuse = namedTag!!.getByte("TNTFuse").toInt()
        } else {
            fuse = 80
        }
        this.setDataFlag(EntityFlag.CHARGED, false)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        // 记录最大高度，用于计算坠落伤害
        if (!this.onGround && position.y > highestPosition) {
            this.highestPosition = position.y
        }
        if (fuse < 80) {
            val tickDiff: Int = currentTick - lastUpdate

            lastUpdate = currentTick

            if (fuse % 5 == 0) {
                setDataProperty(EntityDataTypes.FUSE_TIME, fuse)
            }

            fuse -= tickDiff

            if (isAlive() && fuse <= 0) {
                if (level!!.gameRules.getBoolean(GameRule.TNT_EXPLODES)) {
                    this.explode(ThreadLocalRandom.current().nextInt(5).toDouble())
                }
                this.close()
                return false
            }
        }

        return super.onUpdate(currentTick)
    }

    override fun updateFallState(onGround: Boolean) {
        if (onGround) {
            fallDistance = (this.highestPosition - position.y).toFloat()

            if (fallDistance > 4) {
                if (level!!.gameRules.getBoolean(GameRule.TNT_EXPLODES)) {
                    this.explode(ThreadLocalRandom.current().nextInt(5).toDouble())
                }
                this.resetFallDistance()
                this.close()
            }
        }
    }

    public override fun activate(x: Int, y: Int, z: Int, flag: Boolean) {
        level!!.addSound(this.position, Sound.FIRE_IGNITE)
        this.fuse = 79
    }

    override fun explode() {
        explode(0.0)
    }

    fun explode(square: Double) {
        var root: Double = sqrt(square)

        if (root > 5.0) {
            root = 5.0
        }

        val event: EntityExplosionPrimeEvent =
            EntityExplosionPrimeEvent(this, (4.0 + ThreadLocalRandom.current().nextDouble() * 1.5 * root))
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return
        }
        val explosion: Explosion = Explosion(this.locator, event.force, this)
        explosion.fireChance = event.fireChance
        if (event.isBlockBreaking) {
            explosion.explodeA()
        }
        explosion.explodeB()
        this.close()
    }

    override fun dropItem() {
        if (lastDamageCause is EntityDamageByEntityEvent) {
            val damager: Entity = (lastDamageCause as EntityDamageByEntityEvent).damager
            if (damager is Player && damager.isCreative) {
                return
            }
        }
        level!!.dropItem(this.position, ItemTntMinecart())
    }

    override fun getOriginalName(): String {
        return getType().name
    }

    override fun getType(): MinecartType {
        return MinecartType.valueOf(3)
    }

    override fun saveNBT() {
        super.saveNBT()

        super.namedTag!!.putInt("TNTFuse", this.fuse)
    }


    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        val interact: Boolean = super.onInteract(player, item, clickedPos)
        if (item.id == ItemID.FLINT_AND_STEEL || item.id == ItemID.FIRE_CHARGE) {
            level!!.addSound(this.position, Sound.FIRE_IGNITE)
            this.fuse = 79
            return true
        }

        return interact
    }

    override fun mountEntity(entity: Entity, mode: ActorLink.Companion.Type): Boolean {
        return false
    }
}
