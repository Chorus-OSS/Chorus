package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityIllager
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.ActorEventPacket

class EntityEvocationFang(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityWalkable {
    var evocationIllager: EntityEvocationIllager? = null

    override fun getEntityIdentifier(): String {
        return EntityID.EVOCATION_FANG
    }

    override fun initEntity() {
        this.maxHealth = 1
        super.initEntity()
        level!!.addLevelSoundEvent(
            this.position,
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Fang,
            -1,
            EntityID.EVOCATION_FANG,
            false,
            false
        )
        for (entity in level!!.getCollidingEntities(getBoundingBox())) {
            if (attackTarget(entity)) {
                val event = EntityDamageByEntityEvent(this, entity, DamageCause.MAGIC, 6f)
                entity.attack(event)
            }
        }
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        val pk = ActorEventPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.ArmSwing,
            eventData = 0
        )
        player.sendPacket(pk)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        val ticks = 18 - ticksLived
        setDataProperty(EntityDataTypes.DATA_LIFETIME_TICKS, ticks)
        if (ticks == -1) close()
        return super.onUpdate(currentTick)
    }

    override fun getWidth(): Float {
        return 1f
    }

    override fun getHeight(): Float {
        return 0.8f
    }

    override fun getOriginalName(): String {
        return "Evoker Fang"
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return false
    }

    override fun attackTarget(entity: Entity): Boolean {
        return entity !is EntityIllager
    }
}
