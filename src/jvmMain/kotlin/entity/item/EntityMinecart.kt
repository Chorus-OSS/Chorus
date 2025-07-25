package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.EntitySwimmable
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.MinecartType

class EntityMinecart(chunk: IChunk?, nbt: CompoundTag) : EntityMinecartAbstract(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.MINECART
    }

    override fun getOriginalName(): String {
        return getType().name
    }

    override fun getType(): MinecartType {
        return MinecartType.MINECART_EMPTY
    }

    override fun getInteractButtonText(player: Player): String {
        return "action.interact.ride.minecart"
    }

    override fun isRideable(): Boolean {
        return true
    }

    override fun activate(x: Int, y: Int, z: Int, flag: Boolean) {
        if (flag && this.health > 15 && this.attack(
                EntityDamageByBlockEvent(
                    level!!.getBlock(x, y, z), this, DamageCause.CONTACT, 1f
                )
            )
            && !passengers.isEmpty()
        ) {
            this.dismountEntity(getPassenger()!!)
        }
    }

    override fun onUpdate(currentTick: Int): Boolean {
        var update: Boolean = super.onUpdate(currentTick)

        if (passengers.isEmpty()) {
            for (entity: Entity in level!!.getCollidingEntities(
                boundingBox.grow(0.20000000298023224, 0.0, 0.20000000298023224),
                this
            )) {
                if (entity.riding != null || (entity !is EntityLiving) || entity is Player || entity is EntitySwimmable) {
                    continue
                }

                this.mountEntity(entity)
                update = true
                break
            }
        }

        return update
    }
}
