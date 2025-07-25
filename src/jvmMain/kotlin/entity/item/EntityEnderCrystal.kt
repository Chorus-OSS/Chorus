package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityExplosive
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.monster.EntityEnderDragon
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.Explosion
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityEnderCrystal(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityExplosive {
    override fun getEntityIdentifier(): String {
        return EntityID.ENDER_CRYSTAL
    }

    protected var detonated: Boolean = false

    override fun initEntity() {
        super.initEntity()

        if (namedTag!!.contains("ShowBottom")) {
            this.setShowBase(namedTag!!.getBoolean("ShowBottom"))
        }

        this.fireProof = true
        this.setDataFlag(EntityFlag.FIRE_IMMUNE, true)
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putBoolean("ShowBottom", this.showBase())
    }

    override fun getHeight(): Float {
        return 0.98f
    }

    override fun getWidth(): Float {
        return 0.98f
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (isClosed()) {
            return false
        }

        if (source.cause == DamageCause.FIRE || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.LAVA) {
            return false
        }

        if (!super.attack(source)) {
            return false
        }

        if (source is EntityDamageByEntityEvent) {
            if (source.damager is EntityEnderDragon) {
                return false
            }
        }
        explode()

        return true
    }

    override fun explode() {
        if (!this.detonated) {
            this.detonated = true

            val pos: Locator = this.locator
            val explode: Explosion = Explosion(pos, 6.0, this)

            this.close()

            if (level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)) {
                explode.explodeA()
                explode.explodeB()
            } else {
                explode.explodeB()
            }
        }
    }

    override fun close() {
        super.close()
        for (entity: Entity in level!!.getEntities()) {
            if (entity is EntityEnderDragon) {
                if (entity.position.distance(this.position) <= 28) {
                    entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, 10f))
                }
                entity.memoryStorage[CoreMemoryTypes.LAST_ENDER_CRYSTAL_DESTROY] = position.asBlockVector3()
            }
        }
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return false
    }

    fun showBase(): Boolean {
        return this.getDataFlag(EntityFlag.SHOW_BOTTOM)
    }

    fun setShowBase(value: Boolean) {
        this.setDataFlag(EntityFlag.SHOW_BOTTOM, value)
    }

    fun getBeamTarget(): BlockVector3 {
        return this.getDataProperty<BlockVector3>(EntityDataTypes.BLOCK_TARGET_POS)
    }

    fun setBeamTarget(beamTarget: BlockVector3?) {
        this.setDataProperty(EntityDataTypes.BLOCK_TARGET_POS, beamTarget!!)
    }

    override fun getOriginalName(): String {
        return "Ender Crystal"
    }
}
